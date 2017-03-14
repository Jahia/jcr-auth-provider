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
package org.jahia.modules.jcroauthprovider;

import org.jahia.modules.jahiaoauth.service.JahiaOAuth;

import java.util.List;
import java.util.Map;

/**
 * @author dgaillard
 */
public class DataLoader {

    private String mapperServiceName;
    private JahiaOAuth jahiaOAuth;
    private List<Map<String, Object>> mapperProperties;

    public void onStart() {
        jahiaOAuth.addDataToOAuthMapperPropertiesMap(mapperProperties, mapperServiceName);
    }

    public void setJahiaOAuth(JahiaOAuth jahiaOAuth) {
        this.jahiaOAuth = jahiaOAuth;
    }

    public void setMapperServiceName(String mapperServiceName) {
        this.mapperServiceName = mapperServiceName;
    }

    public String getMapperServiceName() {
        return mapperServiceName;
    }

    public void setMapperProperties(List<Map<String, Object>> mapperProperties) {
        this.mapperProperties = mapperProperties;
    }
}
