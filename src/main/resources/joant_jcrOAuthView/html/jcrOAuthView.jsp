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

<template:addResources type="javascript" resources="jcr-oauth-data-mapper/controller.js"/>

<md-card ng-controller="jcrOAuthDataMapperController">
    <md-card-title>
        <md-card-title-text>
            <span class="md-headline">JCR OAuth data mapper</span>
        </md-card-title-text>
    </md-card-title>

    <md-card-content>
        <md-switch ng-model="isActivate" ng-change="toggleMapper()">
            Activate
        </md-switch>
        <md-input-container>
            <label>Field from connector</label>
            <md-select ng-model="selectedFieldFromConnector">
                <md-optgroup>
                    <md-option ng-repeat="field in connectorFields" ng-value="field" ng-show="isNotMapped(field, 'connector')">
                        {{field}}
                    </md-option>
                </md-optgroup>
            </md-select>
        </md-input-container>
        <md-input-container>
            <label>Field from mapper</label>
            <md-select ng-model="selectedFieldFromMapper">
                <md-optgroup>
                    <md-option ng-repeat="field in mapperFields" ng-value="field" ng-show="isNotMapped(field, 'mapper')">
                        {{field}}
                    </md-option>
                </md-optgroup>
            </md-select>
        </md-input-container>
        <md-button class="md-fab md-mini" ng-click="addMapping()">
            <md-icon>add</md-icon>
        </md-button>

        <md-list>
            <md-list-item ng-repeat="mapping in mappedFields track by $index">
                <md-input-container class="md-block">
                    <label>Connector</label>
                    <input ng-value="mapping.connector" disabled>
                </md-input-container>
                <md-input-container class="md-block">
                    <label>Mapper</label>
                    <input ng-value="mapping.mapper" disabled>
                </md-input-container>
                <md-button class="md-fab md-mini" ng-click="removeMapping($index)">
                    <md-icon>remove</md-icon>
                </md-button>
            </md-list-item>
        </md-list>
    </md-card-content>
<md-card>