package org.jahia.params.valves.jcroauth;

import org.jahia.api.Constants;
import org.jahia.modules.jahiaoauth.service.JahiaOAuth;
import org.jahia.modules.jcroauthdatamapper.DataLoader;
import org.jahia.params.valves.*;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author dgaillard
 */
public class JCROAuthValve extends AutoRegisteredBaseAuthValve {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthValve.class);

    private JahiaUserManagerService jahiaUserManagerService;
    private JCRTemplate jcrTemplate;
    private JahiaOAuth jahiaOAuth;
    private DataLoader dataLoader;
    private CookieAuthConfig cookieAuthConfig;

    public class LoginEvent extends BaseLoginEvent {

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super (source, jahiaUser, authValveContext);
        }
    }

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest request = authContext.getRequest();

        if (authContext.getSessionFactory().getCurrentUser() != null) {
            return;
        }

        final HashMap<String, Object> mapperResult = jahiaOAuth.getMapperResults(dataLoader.getMapperServiceName(), request.getSession().getId());
        if (mapperResult == null) {
            return;
        }

        try {
            jcrTemplate.doExecuteWithSystemSessionAsUser((JahiaUser) null, Constants.EDIT_WORKSPACE, request.getLocale(), new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    String userId = (String) mapperResult.get("j:email");
                    JCRUserNode userNode = jahiaUserManagerService.lookupUser(userId, session);

                    request.getSession().setAttribute(Constants.SESSION_USER, userNode.getJahiaUser());
                    request.setAttribute("login_valve_result", "ok");
                    authContext.getSessionFactory().setCurrentUser(userNode.getJahiaUser());

                    String useCookie = request.getParameter("useCookie");
                    if ((useCookie != null) && ("on".equals(useCookie))) {
                        // the user has indicated he wants to use cookie authentication
                        CookieAuthValveImpl.createAndSendCookie(authContext, userNode, cookieAuthConfig);
                    }

                    SpringContextSingleton.getInstance().publishEvent(new JCROAuthValve.LoginEvent(this, userNode.getJahiaUser(), authContext));

                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage());
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

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }
}
