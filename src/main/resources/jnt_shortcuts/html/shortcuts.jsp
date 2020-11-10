<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<template:addResources type="javascript" resources="jquery.min.js, textsizer.js"/>

<!-- shortcuts -->
<script type="text/javascript">
    $(document).ready(function() {
        $('#shortcuts').children('ul').hide();
        $('#shortcuts').mouseover(function() {
            $(this).children('ul').show();
        }).mouseout(function() {
            $(this).children('ul').hide();
        });

        document.getElementById("print").addEventListener("click", function() {
            window.print();
            return false;
        });
    });
</script>
<div id="shortcuts">
    <h3><a title="Shortcuts" href="#"><fmt:message key="welcome"/></a></h3>
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li>
                <fmt:message key="welcome"/>, <span class="currentUser">${user:fullName(currentUser)}<c:if test="${not empty currentAliasUser}">(&nbsp;<fmt:message key="as.user"/>&nbsp;${currentAliasUser.username})</c:if>:</span>
            </li>
            <li>
                <a class="loginFormTopLogoutShortcuts"
                   href="<c:url value='${url.logout}'/>"><span><fmt:message
                        key="logout"/></span></a>
            </li>
            <c:if test="${!empty url.myProfile}">
                <li class="topshortcuts-mysettings">
                    <a href="<c:url value='${url.myProfile}'/>"><fmt:message key="mySpace.link"/></a>
                </li>
            </c:if>
<%--
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'editModeAccess')}">
                <li>
                    <a href="${url.edit}"><fmt:message key="edit"/></a>
                </li>
            </c:if>
--%>
<%--
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'contributeModeAccess')}">
                <li>
                    <a href="${url.contribute}"><fmt:message key="contribute"/></a>
                </li>
            </c:if>
--%>
        </c:if>
        <li><a href="#" id="print">
            <fmt:message key="print"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
<%--<li>
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.html'/>"><fmt:message key="home"/></a>
        </li>
        <li>
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.sitemap.html'/>"><fmt:message key="sitemap"/></a>
        </li>--%>
    </ul>
</div>
