<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="javascript" resources="i18n/jcr-oauth-data-mapper-i18n_${currentResource.locale}.js" var="i18nJSFile"/>
<c:if test="${empty i18nJSFile}">
    <template:addResources type="javascript" resources="i18n/jcr-oauth-data-mapper-i18n_en.js"/>
</c:if>

<template:addResources>
    <script>
        for (var key in jcroai18n) {
            jahiaOAuthContext.i18n[key] = jcroai18n[key];
        }
    </script>
</template:addResources>

<template:addResources type="javascript" resources="jcr-oauth-data-mapper/controller.js"/>

<md-card ng-controller="jcrOAuthDataMapperController" class="ng-cloak">
    <md-card-title>
        <md-card-title-text>
            <span class="md-headline" message-key="joant_jcrOAuthView"></span>
        </md-card-title-text>
        <md-card-actions layout="row">
            <md-card-icon-actions>
                <md-button class="md-fab" ng-click="saveMapperSettings()">
                    <md-icon>save</md-icon>
                </md-button>
            </md-card-icon-actions>
        </md-card-actions>
    </md-card-title>

    <md-card-content>
        <md-switch ng-model="isActivate">
            <span message-key="joant_jcrOAuthView.label.activate"></span>
        </md-switch>
        <md-input-container>
            <label message-key="joant_jcrOAuthView.label.fieldFromConnector"></label>
            <md-select ng-model="selectedPropertyFromConnector">
                <md-optgroup>
                    <md-option ng-repeat="(propertyKey, propertyValue) in connectorProperties" ng-value="propertyKey" ng-show="isNotMapped(propertyKey, 'connector')">
                        {{ getConnectorI18n(propertyKey) }} ({{ propertyValue['valueType'] }})
                    </md-option>
                </md-optgroup>
            </md-select>
        </md-input-container>
        <md-input-container>
            <label message-key="joant_jcrOAuthView.label.fieldFromMapper"></label>
            <md-select ng-model="selectedPropertyFromMapper">
                <md-optgroup>
                    <md-option ng-repeat="(propertyKey, propertyValue) in mapperProperties" ng-value="propertyKey" ng-show="isNotMapped(propertyKey, 'mapper')">
                        {{ getMapperI18n(propertyKey) }} ({{ propertyValue['valueType'] }}) <span ng-if="propertyValue.mandatory" style="color: red" message-key="joant_jcrOAuthView.label.mandatory"></span>
                    </md-option>
                </md-optgroup>
            </md-select>
        </md-input-container>
        <md-button class="md-fab md-mini" ng-click="addMapping()">
            <md-icon>add</md-icon>
        </md-button>

        <md-list>
            <md-list-item ng-repeat="mapping in mapping track by $index">
                <md-input-container class="md-block">
                    <label message-key="joant_jcrOAuthView.label.connector"></label>
                    <input ng-value="getConnectorI18n(mapping.connector)" disabled>
                </md-input-container>
                <md-input-container class="md-block">
                    <label message-key="joant_jcrOAuthView.label.mapper"></label>
                    <input ng-value="getMapperI18n(mapping.mapper)" disabled>
                </md-input-container>
                <md-button class="md-fab md-mini" ng-click="removeMapping($index)">
                    <md-icon>remove</md-icon>
                </md-button>
            </md-list-item>
        </md-list>
    </md-card-content>
</md-card>