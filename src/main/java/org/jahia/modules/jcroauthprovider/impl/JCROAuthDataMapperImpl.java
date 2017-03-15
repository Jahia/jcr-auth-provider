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
package org.jahia.modules.jcroauthprovider.impl;

import org.apache.jackrabbit.util.ISO8601;
import org.jahia.modules.jahiaoauth.service.Constants;
import org.jahia.modules.jahiaoauth.service.Mapper;
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
                    String userId = (mapperResult.containsKey("j:email")) ? (String) ((Map<String, Object>) mapperResult.get("j:email")).get(Constants.PROPERTY_VALUE) : (String) mapperResult.get(Constants.CONNECTOR_NAME_AND_ID);

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
            if (!entry.getKey().equals(Constants.TOKEN) && !entry.getKey().equals(Constants.CONNECTOR_NAME_AND_ID)) {
                Map<String, Object> propertyInfo = (Map<String, Object>) entry.getValue();
                if (propertyInfo.get(Constants.PROPERTY_VALUE_TYPE).equals("date")) {
                    DateTimeFormatter dtf = DateTimeFormat.forPattern((String) propertyInfo.get(Constants.PROPERTY_VALUE_FORMAT));
                    DateTime date = dtf.parseDateTime((String) propertyInfo.get(Constants.PROPERTY_VALUE));
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTimeInMillis(date.getMillis());
                    userNode.setProperty(entry.getKey(), ISO8601.format(c));
                } else {
                    userNode.setProperty(entry.getKey(), (String) propertyInfo.get(Constants.PROPERTY_VALUE));
                }
            }
        }
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }
}
