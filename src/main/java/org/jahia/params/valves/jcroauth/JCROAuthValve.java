/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.params.valves.jcroauth;

import org.jahia.api.Constants;
import org.jahia.modules.jahiaoauth.service.JahiaOAuth;
import org.jahia.modules.jcroauthprovider.DataLoader;
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
    private JahiaOAuth jahiaOAuth;
    private DataLoader dataLoader;
    private CookieAuthConfig cookieAuthConfig;

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
        HashMap<String, Object> mapperResult = jahiaOAuth.getMapperResults(dataLoader.getMapperServiceName(), originalSessionId);
        if (mapperResult == null || !request.getParameterMap().containsKey("site")) {
            valveContext.invokeNext(context);
            return;
        }

        boolean ok = false;
        String siteKey = request.getParameter("site");
        String userId = (mapperResult.containsKey("j:email")) ? (String) ((Map<String, Object>) mapperResult.get("j:email")).get(org.jahia.modules.jahiaoauth.service.Constants.PROPERTY_VALUE) : (String) mapperResult.get(org.jahia.modules.jahiaoauth.service.Constants.CONNECTOR_NAME_AND_ID);
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
                jahiaOAuth.updateCacheEntry(originalSessionId, request.getSession().getId());
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

    public void setJahiaOAuth(JahiaOAuth jahiaOAuth) {
        this.jahiaOAuth = jahiaOAuth;
    }

    public void setDataLoader(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
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
}
