<%@ page import="org.jahia.services.render.Resource" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="facetParamVarName" value="N-${currentNode.name}"/>
<c:set var="facetTargetType" value="N-type-${currentNode.name}"/>
<c:set target="${moduleMap}" property="editable" value="true" />
<%-- list mode --%>
<c:choose>
    <c:when test="${not empty param[facetParamVarName] or currentResource.moduleParams.queryLoadAllUnsorted == 'true'}">
        <query:definition var="listQuery" >
            <c:choose>
                <c:when test="${not empty param[facetTargetType]}">
                    <query:selector nodeTypeName="${functions:decodeUrlParam(param[facetTargetType])}"/>
                </c:when>
                <c:when test="${not empty currentResource.moduleParams.facetListNodeType}">
                    <query:selector nodeTypeName="${currentResource.moduleParams.facetListNodeType}"/>
                </c:when>
                <c:otherwise>
                    <query:selector nodeTypeName="nt:base"/>
                </c:otherwise>
            </c:choose>
            <c:set var="descendantNode" value="${fn:substringAfter(currentNode.realNode.path,'/sites/')}"/>
            <c:set var="descendantNode" value="${fn:substringAfter(descendantNode,'/')}"/>
            <query:descendantNode path="/sites/${renderContext.site.name}/${descendantNode}"/>
        </query:definition>
        <c:set target="${moduleMap}" property="listQuery" value="${listQuery}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${moduleMap}" property="currentList" value="${jcr:getChildrenOfType(currentNode, jcr:getConstraints(currentNode))}" />
        <c:set target="${moduleMap}" property="end" value="${fn:length(moduleMap.currentList)}" />
        <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}" />
    </c:otherwise>
</c:choose>
