package org.jahia.modules.jcroauthdatamapper;

import org.jahia.modules.jahiaoauth.service.JahiaOAuth;

import java.util.Map;

/**
 * @author dgaillard
 */
public class DataLoader {

    private String mapperServiceName;
    private JahiaOAuth jahiaOAuth;
    private Map<String, Map<String, Object>> mapperProperties;

    public void onStart() {
        jahiaOAuth.addDataToOAuthMapperPropertiesMap(mapperProperties, mapperServiceName);
    }

    public void setMapperProperties(Map<String, Map<String, Object>> mapperProperties) {
        this.mapperProperties = mapperProperties;
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
}
