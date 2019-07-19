<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<%--@elvariable id="memberSearchCriteria" type="org.jahia.services.usermanager.SearchCriteria"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,workInProgress.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<c:set var="multipleProvidersAvailable" value="${fn:length(providers) > 1}"/>

<c:set var="memberCount" value="${fn:length(members)}"/>
<c:set var="membersFound" value="${memberCount > 0}"/>

<c:set var="memberDisplayLimit" value="${properties.memberDisplayLimit}"/>

<c:set var="isGroupEditable" value="${!providers[group.providerName].readOnly}"/>

<c:if test="${flowHandler.searchType eq 'users'}">
    <c:set var="prefix" value="u:"/>
    <c:set var="displayUsers" value="selected"/>
</c:if>
<c:if test="${flowHandler.searchType eq 'groups'}">
    <c:set var="prefix" value="g:"/>
    <c:set var="displayGroups" value="selected"/>
</c:if>

<script type="text/javascript">
    editRoleMembers = {
        addedMembers: [],
        removedMembers: [],
        submitChanges: function () {
            workInProgress('${i18nWaiting}');
            $("#addedMembers").val(editRoleMembers.addedMembers);
            $("#removedMembers").val(editRoleMembers.removedMembers);
            $("#saveForm").submit();
        }
    };

    $(document).ready(function() {
        $(".selectedMember").change(function(event) {
            var name = encodeURIComponent('${prefix}' + $(this).attr('value'));
            if ($(this).is(':checked')) {
                if (editRoleMembers.removedMembers.indexOf(name) > -1) {
                    editRoleMembers.removedMembers.splice(editRoleMembers.removedMembers.indexOf(name),1);
                } else {
                    editRoleMembers.addedMembers[editRoleMembers.addedMembers.length] = name;
                }
            } else {
                if (editRoleMembers.addedMembers.indexOf(name) > -1) {
                    editRoleMembers.addedMembers.splice(editRoleMembers.addedMembers.indexOf(name),1);
                } else {
                    editRoleMembers.removedMembers[editRoleMembers.removedMembers.length] = name;
                }
            }

            if (editRoleMembers.addedMembers.length === 0 && editRoleMembers.removedMembers.length === 0) {
                $('#saveButton').attr('disabled', 'disabled');
            } else {
                $('#saveButton').removeAttr('disabled');
            }
        });

        $('#cbSelectedAllMembers').click(function() {
            var state=this.checked;
            $.each($(':checkbox[name="selectedMembers"]'), function() {
                if (this.checked != state) {
                    this.checked = state;
                    $(this).change();
                }
            });
        });
    })
</script>

<div class="page-header">
    <h2>Role: ${role}</h2>
</div>

<div class="panel panel-default">
    <div class="panel-heading">
        <div class="row">
            <div class="col-md-6">
                <form action="${flowExecutionUrl}" method="post" style="display: inline;">
                    <button class="btn btn-default btn-fab btn-fab-mini" type="submit" name="_eventId_rolesList"
                            data-placement="top" data-container="body"
                            data-toggle="tooltip" data-title="<fmt:message key='default.manageRoles.backToRoles'/>">
                        <i class="material-icons">arrow_back</i>
                    </button>
                </form>
            </div>
            <div class="col-md-6 text-right">
                <form action="${flowExecutionUrl}" method="post" id="saveForm" style="display: inline;">
                    <input id="addedMembers" type="hidden" name="addedMembers"/>
                    <input id="removedMembers" type="hidden" name="removedMembers"/>
                    <input id="eventId" type="hidden" name="_eventId_save" value="on"/>
                    <button class="btn btn-raised btn-primary" type="button" id="saveButton"
                            onclick="editRoleMembers.submitChanges()" disabled="disabled">
                        <fmt:message key="label.save"/>
                    </button>
                </form>

            </div>
        </div>
    </div>

    <div class="panel-body">
        <form id="switchToSelectUsersForm" action="${flowExecutionUrl}"
              method="post" style="display: none">
            <input type="hidden" name="_eventId_users" value="on"/>
        </form>

        <form id="switchToSelectGroupsForm" action="${flowExecutionUrl}"
              method="post" style="display: none">
            <input type="hidden" name="_eventId_groups" value="on"/>
        </form>

        <ul class="nav nav-tabs">
            <li role="presentation" <c:if test="${flowHandler.searchType eq 'users'}"> class="active" </c:if> >
                <a href="#" aria-controls="selectUsers" data-sel-role="switchToUsersView"
                   onclick="$('#switchToSelectUsersForm').submit();return false;"
                   role="tab" data-toggle="tab">
                    <fmt:message key="label.users"/>
                </a>
            </li>
            <li role="presentation" <c:if test="${flowHandler.searchType eq 'groups'}"> class="active" </c:if> >
                <a href="#selectGroups" aria-controls="selectGroups" data-sel-role="switchToGroupsView"
                   onclick="$('#switchToSelectGroupsForm').submit();return false;"
                   role="tab">
                    <fmt:message key="label.groups"/>
                </a>
            </li>
        </ul>
        <br />

        <c:choose>
            <c:when test="${flowHandler.searchType eq 'users'}">
                <p><fmt:message key="default.message.addRemoveUsers"/></p>
            </c:when>
            <c:otherwise>
                <p><fmt:message key="default.message.addRemoveGroups"/></p>
            </c:otherwise>
        </c:choose>

        <form class="form-inline " action="${flowExecutionUrl}" id="searchForm" method="post">
            <input type="hidden" id="searchIn" name="searchIn" value="allProps"/>
            <fieldset>
                <div class="form-group label-floating">
                    <label class="control-label" for="searchString">
                        <fmt:message key="label.search"/>
                    </label>
                    <div class="input-group">
                        <input class="form-control" type="text" id="searchString" name="searchString"
                               value='${memberSearchCriteria.searchString}'
                               onkeydown="if (event.keyCode == 13) submitForm('search');"/>
                        <span class="input-group-btn">
                            <button class="btn btn-primary btn-fab btn-fab-xs" type="submit" name="_eventId_search">
                                <i class="material-icons">search</i>
                            </button>
                        </span>
                    </div>
                </div>
                
                <c:if test="${multipleProvidersAvailable}">
                    <br/>
                    <label for="storedOn"><span class="badge badge-info"><fmt:message
                            key="label.on"/></span></label>
                    <input type="radio" name="storedOn" value="everywhere"
                        ${empty memberSearchCriteria.storedOn || memberSearchCriteria.storedOn == 'everywhere' ? ' checked="checked" ' : ''}
                           onclick="$('.provCheck').attr('disabled',true);">&nbsp;<fmt:message
                        key="label.everyWhere"/>
    
                    <input type="radio" name="storedOn" value="providers"
                        ${memberSearchCriteria.storedOn == 'providers' ? 'checked="checked"' : ''}
                           onclick="$('.provCheck').removeAttr('disabled');"/>&nbsp;<fmt:message
                        key="label.providers"/>
    
                    <c:forEach items="${providers}" var="curProvider">
                        <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                            ${memberSearchCriteria.storedOn != 'providers' ? 'disabled="disabled"' : ''}
                            ${empty memberSearchCriteria.providers || functions:contains(memberSearchCriteria.providers, curProvider.key) ? 'checked="checked"' : ''}/>
                        <fmt:message var="i18nProviderLabel" key="providers.${curProvider.key}.label"/>
                        ${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? curProvider.key : i18nProviderLabel)}
                    </c:forEach>
                </c:if>
            </fieldset>
        </form>

        <c:set var="principalsCount" value="${fn:length(principals)}"/>
        <c:set var="principalsFound" value="${principalsCount > 0}"/>
    
        <c:if test="${principalsCount > memberDisplayLimit}">
            <div class="alert alert-info">
                <fmt:message key="default.manageRoles.${flowHandler.searchType}.found">
                    <fmt:param value="${principalsCount}"/>
                    <fmt:param value="${memberDisplayLimit}"/>
                </fmt:message>
            </div>
        </c:if>
    
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th width="2%">
                    <div class="checkbox">
                        <label>
                            <input type="checkbox" name="selectedAllMembers" 
                                   id="cbSelectedAllMembers"/>
                        </label>
                    </div>
                </th>
                <th><fmt:message key="label.username"/></th>
                <c:if test="${multipleProvidersAvailable}">
                    <th width="10%"><fmt:message key="column.provider.label"/></th>
                </c:if>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${!principalsFound}">
                    <tr>
                        <td colspan="${multipleProvidersAvailable ? '3' : '2'}"><fmt:message key="label.noItemFound"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${principals}" var="principal" end="${memberDisplayLimit - 1}" varStatus="loopStatus">
                        <tr>
                            <td>
                                <div class="checkbox">
                                    <label>
                                        <input class="selectedMember"
                                               type="checkbox" name="selectedMembers" 
                                               value="${fn:escapeXml(principal.name)}" ${functions:contains(members, principal) ? 'checked="checked"' : ''}/>
                                    </label>
                                </div>
                            </td>
                            <td>
                                    ${fn:escapeXml(user:displayName(principal))}
                            </td>
                            <c:if test="${multipleProvidersAvailable}">
                                <fmt:message var="i18nProviderLabel" key="providers.${principal.providerName}.label"/>
                                <td>${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? principal.providerName : i18nProviderLabel)}</td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
