/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.jcroauthprovider.impl;

import org.apache.jackrabbit.util.ISO8601;
import org.jahia.modules.jahiaoauth.service.JahiaOAuthConstants;
import org.jahia.modules.jahiaoauth.service.MapperService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * @author dgaillard
 */
public class JCROAuthProviderMapperImpl implements MapperService {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthProviderMapperImpl.class);

    private JahiaUserManagerService jahiaUserManagerService;
    private JCRTemplate jcrTemplate;
    private List<Map<String, Object>> properties;
    private String serviceName;

    @Override
    public List<Map<String, Object>> getProperties() {
        return properties;
    }

    @Override
    public void executeMapper(final Map<String, Object> mapperResult) {
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    String userId = (mapperResult.containsKey("j:email")) ? (String) ((Map<String, Object>) mapperResult.get("j:email")).get(JahiaOAuthConstants.PROPERTY_VALUE) : (String) mapperResult.get(JahiaOAuthConstants.CONNECTOR_NAME_AND_ID);

                    JCRUserNode userNode = jahiaUserManagerService.lookupUser(userId, session);
                    if (userNode == null) {
                        Properties properties = new Properties();
                        userNode = jahiaUserManagerService.createUser(userId, "SHA-1:*", properties, session);
                        updateUserProperties(userNode, mapperResult);
                        if (userNode == null) {
                            throw new RuntimeException("Cannot create user from access token");
                        }
                    } else {
                        try {
                            updateUserProperties(userNode, mapperResult);
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

    private void updateUserProperties(JCRUserNode userNode, Map<String, Object> mapperResult) throws RepositoryException {
        for (Map.Entry<String, Object> entry : mapperResult.entrySet()) {
            if (!entry.getKey().equals(JahiaOAuthConstants.TOKEN_DATA)
                    && !entry.getKey().equals(JahiaOAuthConstants.CONNECTOR_NAME_AND_ID)
                    && !entry.getKey().equals(JahiaOAuthConstants.PROPERTY_SITE_KEY)
                    && !entry.getKey().equals(JahiaOAuthConstants.CONNECTOR_SERVICE_NAME)) {
                Map<String, Object> propertyInfo = (Map<String, Object>) entry.getValue();
                if (propertyInfo.get(JahiaOAuthConstants.PROPERTY_VALUE_TYPE).equals("date")) {
                    DateTimeFormatter dtf = DateTimeFormat.forPattern((String) propertyInfo.get(JahiaOAuthConstants.PROPERTY_VALUE_FORMAT));
                    DateTime date = dtf.parseDateTime((String) propertyInfo.get(JahiaOAuthConstants.PROPERTY_VALUE));
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTimeInMillis(date.getMillis());
                    userNode.setProperty(entry.getKey(), ISO8601.format(c));
                } else {
                    userNode.setProperty(entry.getKey(), (String) propertyInfo.get(JahiaOAuthConstants.PROPERTY_VALUE));
                }
            }
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setProperties(List<Map<String, Object>> properties) {
        this.properties = properties;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }
}
