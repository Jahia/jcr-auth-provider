/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p>
 * http://www.jahia.com
 * <p>
 * Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 * <p>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p>
 * 1/ GPL
 * ==================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.jcroauthprovider.impl;

import org.apache.jackrabbit.util.ISO8601;
import org.jahia.api.content.JCRTemplate;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.modules.jahiaauth.service.*;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRUserNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author dgaillard
 */
public class JCROAuthProviderMapperImpl implements Mapper {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthProviderMapperImpl.class);
    private static final String PROP_CREATE_USER_AT_SITE_LEVEL = "createUserAtSiteLevel";
    private static final String EMPTY_PASSWORD = "SHA-1:*";
    private JahiaUserManagerService jahiaUserManagerService;
    private JCRTemplate jcrTemplate;
    private List<MappedPropertyInfo> properties;
    private String serviceName;

    @Override
    public List<MappedPropertyInfo> getProperties() {
        return properties;
    }

    public void setProperties(List<MappedPropertyInfo> properties) {
        this.properties = properties;
    }

    @Override
    public void executeMapper(final Map<String, MappedProperty> mapperResult, MapperConfig config) {
        final MappedProperty userIdProp = mapperResult.get(JahiaAuthConstants.SSO_LOGIN);
        if (userIdProp == null) {
            return;
        }

        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    String userId = (String) userIdProp.getValue();

                    JCRUserNode userNode = jahiaUserManagerService.lookupUser(userId, session);
                    if (userNode == null) {
                        final Boolean createUserAtSiteLevel = config.getBooleanProperty(PROP_CREATE_USER_AT_SITE_LEVEL);
                        Properties userProperties = new Properties();
                        if (createUserAtSiteLevel) {
                            final String siteKey = config.getSiteKey();
                            userNode = jahiaUserManagerService.createUser(userId, siteKey, EMPTY_PASSWORD, userProperties, session);
                        } else {
                            userNode = jahiaUserManagerService.createUser(userId, EMPTY_PASSWORD, userProperties, session);
                        }
                        if (userNode == null) {
                            throw new RuntimeException("Cannot create user from access token");
                        }
                        updateUserProperties(userNode, mapperResult);
                    } else {
                        try {
                            updateUserProperties(userNode, mapperResult);
                        } catch (RepositoryException e) {
                            logger.error("Could not set user property {}", e.getMessage());
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

    private void updateUserProperties(JCRUserNode userNode, Map<String, MappedProperty> mapperResult) throws RepositoryException {
        for (Map.Entry<String, MappedProperty> entry : mapperResult.entrySet()) {
            if (!entry.getKey().equals(JahiaAuthConstants.SITE_KEY) && !entry.getKey().equals(JahiaAuthConstants.SSO_LOGIN)) {
                MappedProperty property = entry.getValue();
                if (property.getInfo().getValueType().equals("date")) {
                    DateTimeFormatter dtf = DateTimeFormat.forPattern(property.getInfo().getFormat());
                    DateTime date = dtf.parseDateTime((String) property.getValue());
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTimeInMillis(date.getMillis());
                    userNode.setProperty(entry.getKey(), ISO8601.format(c));
                } else {
                    userNode.setProperty(entry.getKey(), (String) property.getValue());
                }
            }
        }
    }

    public String getServiceName() {
        return serviceName;
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
