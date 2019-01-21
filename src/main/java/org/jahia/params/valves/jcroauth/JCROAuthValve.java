/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.params.valves.jcroauth;

import org.jahia.api.Constants;
import org.jahia.modules.jahiaoauth.service.JahiaOAuthCacheService;
import org.jahia.modules.jahiaoauth.service.JahiaOAuthConstants;
import org.jahia.modules.jahiaoauth.service.JahiaOAuthService;
import org.jahia.modules.jcroauthprovider.impl.JCROAuthProviderMapperImpl;
import org.jahia.params.valves.*;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author dgaillard
 */
public class JCROAuthValve extends AutoRegisteredBaseAuthValve {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthValve.class);
    private static String VALVE_RESULT = "login_valve_result";

    private JahiaUserManagerService jahiaUserManagerService;
    private JahiaOAuthService jahiaOAuthService;
    private JahiaOAuthCacheService jahiaOAuthCacheService;
    private CookieAuthConfig cookieAuthConfig;
    private JCROAuthProviderMapperImpl jcrOAuthProviderMapperImpl;

    private String preserveSessionAttributes = null;

    public class LoginEvent extends BaseLoginEvent {
        private static final long serialVersionUID = 8966163034180261958L;

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super (source, jahiaUser, authValveContext);
        }
    }

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();

        if (authContext.getSessionFactory().getCurrentUser() != null) {
            valveContext.invokeNext(context);
            return;
        }

        String originalSessionId = request.getSession().getId();
        HashMap<String, Object> mapperResult = jahiaOAuthService.getMapperResults(jcrOAuthProviderMapperImpl.getServiceName(), originalSessionId);
        if (mapperResult == null || !request.getParameterMap().containsKey("site")) {
            valveContext.invokeNext(context);
            return;
        }

        boolean ok = false;
        String siteKey = request.getParameter("site");
        String userId = (mapperResult.containsKey("j:email")) ? (String) ((Map<String, Object>) mapperResult.get("j:email")).get(JahiaOAuthConstants.PROPERTY_VALUE) : (String) mapperResult.get(JahiaOAuthConstants.CONNECTOR_NAME_AND_ID);
        JCRUserNode userNode = jahiaUserManagerService.lookupUser(userId, siteKey);

        if (userNode != null) {
            if (!userNode.isAccountLocked()) {
                ok = true;
            } else {
                logger.warn("Login failed: account for user " + userNode.getName() + " is locked.");
                request.setAttribute(VALVE_RESULT, "account_locked");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Login failed. Unknown username " + userId);
            }
            request.setAttribute(VALVE_RESULT, "unknown_user");
        }

        if (ok) {
            if (logger.isDebugEnabled()) {
                logger.debug("User " + userNode + " logged in.");
            }

            // if there are any attributes to conserve between session, let's copy them into a map first
            Map<String, Object> savedSessionAttributes = preserveSessionAttributes(request);

            JahiaUser jahiaUser = userNode.getJahiaUser();

            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            if (!originalSessionId.equals(request.getSession().getId())) {
                jahiaOAuthCacheService.updateCacheEntry(originalSessionId, request.getSession().getId());
            }

            // if there were saved session attributes, we restore them here.
            restoreSessionAttributes(request, savedSessionAttributes);

            request.setAttribute(VALVE_RESULT, "ok");
            authContext.getSessionFactory().setCurrentUser(jahiaUser);

            // do a switch to the user's preferred language
            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(userNode, LanguageCodeConverters.resolveLocaleForGuest(request));
                request.getSession().setAttribute(Constants.SESSION_LOCALE, preferredUserLocale);
            }

            String useCookie = request.getParameter("useCookie");
            if ((useCookie != null) && ("on".equals(useCookie))) {
                // the user has indicated he wants to use cookie authentication
                CookieAuthValveImpl.createAndSendCookie(authContext, userNode, cookieAuthConfig);
            }

            SpringContextSingleton.getInstance().publishEvent(new JCROAuthValve.LoginEvent(this, jahiaUser, authContext));
        } else {
            valveContext.invokeNext(context);
        }
    }

    private Map<String, Object> preserveSessionAttributes(HttpServletRequest httpServletRequest) {
        Map<String,Object> savedSessionAttributes = new HashMap<>();
        if ((preserveSessionAttributes != null) &&
                (httpServletRequest.getSession(false) != null) &&
                (preserveSessionAttributes.length() > 0)) {
            String[] sessionAttributeNames = Patterns.TRIPLE_HASH.split(preserveSessionAttributes);
            HttpSession session = httpServletRequest.getSession(false);
            for (String sessionAttributeName : sessionAttributeNames) {
                Object attributeValue = session.getAttribute(sessionAttributeName);
                if (attributeValue != null) {
                    savedSessionAttributes.put(sessionAttributeName, attributeValue);
                }
            }
        }
        return savedSessionAttributes;
    }

    private void restoreSessionAttributes(HttpServletRequest httpServletRequest, Map<String, Object> savedSessionAttributes) {
        if (savedSessionAttributes.size() > 0) {
            HttpSession session = httpServletRequest.getSession();
            for (Map.Entry<String, Object> savedSessionAttribute : savedSessionAttributes.entrySet()) {
                session.setAttribute(savedSessionAttribute.getKey(), savedSessionAttribute.getValue());
            }
        }
    }

    public void setJahiaOAuthService(JahiaOAuthService jahiaOAuthService) {
        this.jahiaOAuthService = jahiaOAuthService;
    }

    public void setJahiaOAuthCacheService(JahiaOAuthCacheService jahiaOAuthCacheService) {
        this.jahiaOAuthCacheService = jahiaOAuthCacheService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setPreserveSessionAttributes(String preserveSessionAttributes) {
        this.preserveSessionAttributes = preserveSessionAttributes;
    }

    public void setJcrOAuthProviderMapperImpl(JCROAuthProviderMapperImpl jcrOAuthProviderMapperImpl) {
        this.jcrOAuthProviderMapperImpl = jcrOAuthProviderMapperImpl;
    }
}
