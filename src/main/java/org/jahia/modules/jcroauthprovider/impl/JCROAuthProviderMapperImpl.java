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

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.modules.jahiaauth.service.JahiaAuthConstants;
import org.jahia.modules.jahiaauth.service.MappedProperty;
import org.jahia.modules.jahiaauth.service.MappedPropertyInfo;
import org.jahia.modules.jahiaauth.service.Mapper;
import org.jahia.modules.jahiaauth.service.MapperConfig;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author dgaillard
 */
@Component(service = Mapper.class, immediate = true)
public class JCROAuthProviderMapperImpl implements Mapper {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthProviderMapperImpl.class);
    private static final String PROP_CREATE_USER_AT_SITE_LEVEL = "createUserAtSiteLevel";
    private static final String PROP_CREATE_USER_AT_SERVER_LEVEL = "createUserAtServerLevel";
    private static final String EMPTY_P = "SHA-1:*";

    @Reference
    private JahiaUserManagerService jahiaUserManagerService;

    private List<MappedPropertyInfo> properties;

    @Activate
    public void activate(Map<String, ?> properties) {
        modified(properties);
    }

    @Modified
    public void modified(Map<String, ?> properties) {
        // At module startup, config is not read yet
        if (properties == null || properties.get("mappings") == null) {
            logger.info("JCR Auth provider started. No mappings defined.");
            this.properties = new ArrayList<>();
            return;
        }
        String[] mappings = StringUtils.split((String) properties.get("mappings"), ",");
        logger.info("JCR Auth provider config update. {} mappings updated.", mappings.length);
        String[] mandatoryMappings = StringUtils.split((String) properties.get("mappings.mandatory"), ",");
        List<MappedPropertyInfo> newProperties = new ArrayList<>();
        // Fill the properties
        for (String mapping : mappings) {
            MappedPropertyInfo mappedPropertyInfo;
            if (mapping.contains("|")) {
                String name = StringUtils.substringBefore(mapping, "|");
                String type = StringUtils.substringAfter(mapping, "|");
                boolean mandatory = Arrays.asList(mandatoryMappings).contains(name);
                mappedPropertyInfo = new MappedPropertyInfo(name, type, null, mandatory);
            } else {
                boolean mandatory = Arrays.asList(mandatoryMappings).contains(mapping);
                mappedPropertyInfo = new MappedPropertyInfo(mapping, "string", null, mandatory);
            }
            newProperties.add(mappedPropertyInfo);
        }
        this.properties = newProperties;
    }

    @Override
    public List<MappedPropertyInfo> getProperties() {
        return properties;
    }

    @Override
    public void executeMapper(final Map<String, MappedProperty> mapperResult, MapperConfig config) {
        final MappedProperty userIdProp = mapperResult.get(JahiaAuthConstants.SSO_LOGIN);
        if (userIdProp == null) {
            return;
        }

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                executeMapperWithJCRSession(mapperResult, config, session, userIdProp);
                return null;
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage());
        }
    }

    private void executeMapperWithJCRSession(Map<String, MappedProperty> mapperResult, MapperConfig config, JCRSessionWrapper session, MappedProperty userIdProp) throws RepositoryException {
        String userId = (String) userIdProp.getValue();

        // Lookup user at global level
        JCRUserNode userNode = jahiaUserManagerService.lookupUser(userId, session);

        final String siteKey = config.getSiteKey();

        // Lookup user at site level
        if (userNode == null) {
            userNode = jahiaUserManagerService.lookupUser(userId, siteKey, session);
        }

        // If user is missing, we create it
        if (userNode == null) {
            Properties userProperties = new Properties();

            // Will be false if the property is not defined/null
            boolean createUserAtSiteLevel = config.getBooleanProperty(PROP_CREATE_USER_AT_SITE_LEVEL);
            boolean createUserAtServerLevel = config.getBooleanProperty(PROP_CREATE_USER_AT_SERVER_LEVEL);
            if (createUserAtSiteLevel) {
                userNode = jahiaUserManagerService.createUser(userId, siteKey, EMPTY_P, userProperties, session);
            } else if (createUserAtServerLevel) {
                userNode = jahiaUserManagerService.createUser(userId, EMPTY_P, userProperties, session);
            } else {
                logger.info("User {} not found", userId);
            }
            if (createUserAtSiteLevel || createUserAtServerLevel) {
                if (userNode == null) {
                    throw new JahiaRuntimeException("Cannot create user from access token");
                }
                org.jahia.services.usermanager.JahiaUserManagerService.getInstance().clearNonExistingUsersCache();
                updateUserProperties(userNode, mapperResult);
            }
        } else {
            try {
                updateUserProperties(userNode, mapperResult);
            } catch (RepositoryException e) {
                logger.error("Could not set user property {}", e.getMessage());
            }
        }
        session.save();
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
}
