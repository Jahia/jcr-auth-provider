package org.jahia.modules.jcroauthdatamapper.impl;

import org.jahia.modules.jahiaoauth.service.Mapper;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.Properties;

/**
 * @author dgaillard
 */
public class JCROAuthDataMapperImpl implements Mapper {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthDataMapperImpl.class);

    private JahiaUserManagerService jahiaUserManagerService;
    private JCRTemplate jcrTemplate;

    @Override
    public void executeMapper(final Map<String, Object> mapperResult) {
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    String userId = (String) mapperResult.get("j:email");
                    JCRUserNode userNode = jahiaUserManagerService.lookupUser(userId, session);
                    if (userNode == null) {
                        Properties properties = new Properties();
                        for (Map.Entry<String, Object> entry : mapperResult.entrySet()) {
                            properties.setProperty(entry.getKey(), (String) entry.getValue());
                        }
                        userNode = jahiaUserManagerService.createUser(userId, "SHA-1:*", properties, session);
                        if (userNode == null) {
                            throw new RuntimeException("Cannot create user from access token");
                        }
                    } else {
                        try {
                            for (Map.Entry<String, Object> entry : mapperResult.entrySet()) {
                                userNode.setProperty(entry.getKey(), (String) entry.getValue());
                            }
                        } catch (RepositoryException e) {
                            logger.error("Could not set user property", e.getMessage());
                        }
                    }

                    session.save();

                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage());
        }
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }
}
