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

<template:addResources type="javascript" resources="i18n/jcr-auth-provider-i18n_${renderContext.UILocale}.js" var="i18nJSFile"/>
<c:if test="${empty i18nJSFile}">
    <template:addResources type="javascript" resources="i18n/jcr-auth-provider-i18n_en.js"/>
</c:if>

<template:addResources type="javascript" resources="jcr-auth-provider/jcr-mapper-controller.js"/>

<md-card ng-controller="JCROAuthProviderController as jcrOAuthProvider" class="ng-cloak">
    <div layout="row">
        <md-card-title flex>
            <md-card-title-text>
                <span class="md-headline" message-key="joant_jcrOAuthView"></span>
            </md-card-title-text>
        </md-card-title>
        <div flex layout="row" layout-align="end center">
            <md-button class="md-icon-button" ng-click="jcrOAuthProvider.toggleCard()">
                <md-tooltip md-direction="top">
                    <span message-key="joant_jcrOAuthView.tooltip.toggleSettings"></span>
                </md-tooltip>
                <md-icon ng-show="!jcrOAuthProvider.expandedCard">
                    keyboard_arrow_down
                </md-icon>
                <md-icon ng-show="jcrOAuthProvider.expandedCard">
                    keyboard_arrow_up
                </md-icon>
            </md-button>
        </div>
    </div>

    <md-card-content ng-show="jcrOAuthProvider.expandedCard">

        <div class="md-subhead joa-description">
            <span message-key="joant_jcrOAuthView.message.description1"></span>
            <br />
            <span message-key="joant_jcrOAuthView.message.description2"></span>
            <br />
            <span message-key="joant_jcrOAuthView.message.description3"></span>
        </div>

        <div layout="row" layout-align="space-between center">
            <md-switch ng-model="jcrOAuthProvider.enabled">
                <span message-key="joant_jcrOAuthView.label.activate"></span>
            </md-switch>

            <md-switch ng-model="jcrOAuthProvider.createUserAtSiteLevel" ng-true-value="'true'" ng-false-value="'false'">
                <span message-key="joant_jcrOAuthView.label.createUserAtSiteLevel"></span>
            </md-switch>
            <md-input-container>
                <label message-key="joant_jcrOAuthView.label.fieldFromConnector"></label>
                <md-select ng-model="jcrOAuthProvider.selectedPropertyFromConnector" ng-change="jcrOAuthProvider.addMapping()">
                    <md-optgroup>
                        <md-option ng-repeat="connectorProperty in jcrOAuthProvider.connectorProperties | orderBy:jcrOAuthProvider.orderByConnector" ng-value="connectorProperty" val="{{connectorProperty.name}}">
                            {{ jcrOAuthProvider.getConnectorI18n(connectorProperty.name) }}
                        </md-option>
                    </md-optgroup>
                </md-select>
            </md-input-container>
        </div>


        <section ng-show="jcrOAuthProvider.mapping.length > 0">
            <hr />
            <div layout="row" ng-repeat="mapped in jcrOAuthProvider.mapping track by $index" layout-align="start center">
                <div flex="45" ng-if="mapped.editable">
                    <input ng-model="mapped.connector.name"/>
                </div>
                <div flex="45" ng-if="!mapped.editable">
                    {{ jcrOAuthProvider.getConnectorI18n(mapped.connector.name) }}
                </div>
                <div flex="45" layout="row">
                    <md-input-container flex>
                        <label message-key="joant_jcrOAuthView.label.fieldFromMapper"></label>
                        <md-select ng-if="!mapped.editable" ng-model="mapped.mapper" ng-model-options="{trackBy: '$value.name'}">
                            <md-optgroup>
                                <md-option ng-repeat="mapperProperty in jcrOAuthProvider.mapperProperties | selectable:{mapping:jcrOAuthProvider.mapping,key:'mapper',selected:mapped.mapper} | typeMatch:mapped.connector.valueType | orderBy:jcrOAuthProvider.orderByMapper" ng-value="mapperProperty" val="{{mapperProperty.name}}">
                                    {{ jcrOAuthProvider.getMapperI18n(mapperProperty.name) }} <span ng-if="mapperProperty.mandatory" class="joa-mandatory-property" message-key="joant_jcrOAuthView.label.mandatory"></span>
                                </md-option>
                            </md-optgroup>
                        </md-select>
                        <md-select ng-if="mapped.editable" ng-model="mapped.mapper" ng-model-options="{trackBy: '$value.name'}">
                            <md-optgroup>
                                <md-option ng-repeat="mapperProperty in jcrOAuthProvider.mapperProperties | selectable:{mapping:jcrOAuthProvider.mapping,key:'mapper',selected:mapped.mapper} | orderBy:jcrOAuthProvider.orderByMapper" ng-value="mapperProperty" val="{{mapperProperty.name}}">
                                    {{ jcrOAuthProvider.getMapperI18n(mapperProperty.name) }} (<span message-key="joant_jcrOAuthView.label.expectedType"></span> {{ mapperProperty.valueType }}) <span ng-if="mapperProperty.mandatory" class="joa-mandatory-property" message-key="joant_jcrOAuthView.label.mandatory"></span>
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
                               ng-click="jcrOAuthProvider.removeMapping($index)">
                        <md-tooltip md-direction="left">
                            <span message-key="joant_jcrOAuthView.tooltip.removeMappedField"></span>
                        </md-tooltip>
                        <md-icon>remove_circle_outline</md-icon>
                    </md-button>
                </div>
            </div>
        </section>

        <md-card-actions layout="row" layout-align="end center">
            <md-button class="md-accent" message-key="joant_jcrOAuthView.label.save" data-sel-role="saveMappings"
                       ng-click="jcrOAuthProvider.saveMapperSettings()">
            </md-button>
        </md-card-actions>
    </md-card-content>
</md-card>
