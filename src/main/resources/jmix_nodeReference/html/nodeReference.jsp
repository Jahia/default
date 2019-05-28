<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="jahia" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<c:set var="view" value="${currentNode.properties['j:referenceView'].string}"/>
<c:if test="${empty view}">
    <c:set var="view" value="${currentNode.parent.properties['j:subNodesView'].string}"/>
</c:if>
<c:if test="${empty view}">
    <c:set var="view" value="default"/>
</c:if>
<c:catch var="exception">
    <jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
    <c:set var="node" value="${reference.node}"/>
    <c:choose>
        <c:when test="${not empty node}">
            <template:addCacheDependency uuid="${currentNode.properties['j:node'].string}"/>
            <template:module node="${currentNode.contextualizedNode}" editable="false" view="${view}">
                <template:param name="refTitle" value="${currentNode.properties['jcr:title'].string}"/>
            </template:module>
        </c:when>
        <c:otherwise>
            <c:if test="${not empty reference}">
                <jahia:addCacheDependency path="${reference.string}" />
            </c:if>
            <c:if test="${renderContext.editMode}">
                <fmt:message key="label.missingReference"/>
            </c:if>
        </c:otherwise>
    </c:choose>
</c:catch>
<c:if test="${!empty exception}">${exception.message}</c:if>