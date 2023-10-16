<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="pagecategorizing.css"/>
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent}">
    <template:addResources type="javascript" resources="apps/default.jquery.bundle.js,apps/default.categories.bundle.js"/>
    <div class="categorythispage">
        <jcr:nodeProperty node="${boundComponent}" name="j:defaultCategory" var="assignedCategories"/>
        <c:set var="uuid" value="${boundComponent.identifier}"/>
        <c:set var="nodeLocked" value="${jcr:isLockedAndCannotBeEdited(boundComponent)}"/>
        <c:if test="${not nodeLocked}">
            <c:url var="postUrl" value="${url.base}${boundComponent.path}"/>
            <script type="text/javascript">
                var uuids = [];
                <c:forEach items="${assignedCategories}" var="category">
                    <c:if test="${not empty category.node}">
                        uuids.push("${category.node.identifier}");
                    </c:if>
                </c:forEach>
                categoriesLib.initDisplay('${uuid}', uuids, '${postUrl}');
            </script>
        </c:if>

        <jsp:useBean id="filteredCategories" class="java.util.LinkedHashMap"/>
        <c:forEach items="${assignedCategories}" var="category" varStatus="status">
            <c:if test="${not empty category.node}">
                <c:set target="${filteredCategories}" property="${category.node.identifier}"
                       value="${category.node.properties['jcr:title'].string}"/>
            </c:if>
        </c:forEach>
        <div class="categorized">
            <span><fmt:message key="label.categories"/>:</span>
            <span id="jahia-categories-${uuid}">
                <c:choose>
                    <c:when test="${not empty filteredCategories}">
                        <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ' ,')}"/>
                        <c:forEach items="${filteredCategories}" var="category" varStatus="status">
                            <div id="category${category.key}" style="display:inline">
                                    ${!status.first ? separator : ''}<span
                                    class="categorizeditem">${fn:escapeXml(category.value)}</span>
                                <c:if test="${not nodeLocked}">
                                    <a class="delete deleteCategory" id="${category.key}"  href="#"></a>
                                </c:if>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="nocategorizeditem${uuid}">
                            <fmt:message key="label.categories.noCategory"/>
                        </span>
                    </c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>
</c:if>
