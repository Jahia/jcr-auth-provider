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

<template:addResources type="javascript" resources="jcr-oauth-data-mapper/jcr-mapper-controller.js"/>

<md-card ng-controller="JCROAuthDataMapperController as jcrOAuthDataMapper" class="ng-cloak">
    <div layout="row">
        <md-card-title flex>
            <md-card-title-text>
                <span class="md-headline" message-key="joant_jcrOAuthView"></span>
            </md-card-title-text>
        </md-card-title>
        <div flex layout="row" layout-align="end center">
            <md-button class="md-icon-button" ng-click="jcrOAuthDataMapper.toggleCard()">
                <md-tooltip md-direction="top">
                    <span message-key="joant_jcrOAuthView.tooltip.toggleSettings"></span>
                </md-tooltip>
                <md-icon ng-show="!jcrOAuthDataMapper.expandedCard">
                    keyboard_arrow_down
                </md-icon>
                <md-icon ng-show="jcrOAuthDataMapper.expandedCard">
                    keyboard_arrow_up
                </md-icon>
            </md-button>
        </div>
    </div>

    <md-card-content ng-show="jcrOAuthDataMapper.expandedCard">

        <div class="md-subhead joa-description" message-key="joant_jcrOAuthView.message.description"></div>

        <div flex="35" layout="row" layout-align="start center">
            <md-switch ng-model="jcrOAuthDataMapper.isActivate">
                <span message-key="joant_jcrOAuthView.label.activate"></span>
            </md-switch>

            <div flex="10"></div>

            <md-input-container flex>
                <label message-key="joant_jcrOAuthView.label.fieldFromConnector"></label>
                <md-select ng-model="jcrOAuthDataMapper.selectedPropertyFromConnector" ng-change="jcrOAuthDataMapper.addMapping()">
                    <md-optgroup>
                        <md-option ng-repeat="connectorProperty in jcrOAuthDataMapper.connectorProperties" ng-value="connectorProperty" ng-show="jcrOAuthDataMapper.isNotMapped(connectorProperty.name, 'connector')">
                            {{ jcrOAuthDataMapper.getConnectorI18n(connectorProperty.name) }}
                        </md-option>
                    </md-optgroup>
                </md-select>
            </md-input-container>
        </div>


        <section ng-show="jcrOAuthDataMapper.mapping.length > 0">
            <hr />
            <div layout="row" ng-repeat="mapped in jcrOAuthDataMapper.mapping track by $index" layout-align="start center">
                <div flex="45">
                    {{ jcrOAuthDataMapper.getConnectorI18n(mapped.connector.name) }}
                </div>
                <div flex="45" layout="row">
                    <md-input-container flex>
                        <label message-key="joant_jcrOAuthView.label.fieldFromMapper"></label>
                        <md-select ng-model="mapped.mapper" ng-model-options="{trackBy: '$value.name'}">
                            <md-optgroup>
                                <md-option ng-repeat="mapperProperty in jcrOAuthDataMapper.mapperProperties" ng-value="mapperProperty" ng-show="jcrOAuthDataMapper.isNotMapped(mapperProperty.name, 'mapper')">
                                    {{ jcrOAuthDataMapper.getMapperI18n(mapperProperty.name) }} <span ng-if="mapperProperty.mandatory" class="joa-mandatory-property" message-key="joant_jcrOAuthView.label.mandatory"></span>
                                </md-option>
                            </md-optgroup>
                        </md-select>
                    </md-input-container>
                </div>
                <div flex="10" layout="row" layout-align="end center">
                    <md-button class="md-icon-button"
                               ng-class="{ 'md-warn': hover }"
                               ng-mouseenter="hover = true"
                               ng-mouseleave="hover = false"
                               ng-click="jcrOAuthDataMapper.removeMapping($index)">
                        <md-tooltip md-direction="left">
                            <span message-key="joant_jcrOAuthView.tooltip.removeMappedField"></span>
                        </md-tooltip>
                        <md-icon>remove_circle_outline</md-icon>
                    </md-button>
                </div>
            </div>
        </section>

        <md-card-actions layout="row" layout-align="end center">
            <md-button class="md-accent" message-key="joant_jcrOAuthView.label.save"
                       ng-click="jcrOAuthDataMapper.saveMapperSettings()">
            </md-button>
        </md-card-actions>
    </md-card-content>
</md-card>