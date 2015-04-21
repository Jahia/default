<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="nodeTypes" value="${functions:default(currentResource.moduleParams.nodeTypes, param.nodeTypes)}"/>
<c:set var="selectableNodeTypes" value="${functions:default(currentResource.moduleParams.selectableNodeTypes, param.selectableNodeTypes)}"/>
<c:if test="${empty nodeTypes || jcr:isNodeType(currentNode, nodeTypes)}">
<json:object>
	<json:property name="id" value="${currentNode.identifier}"/>
	<json:property name="path" value="${currentNode.path}"/>
	<json:property name="text" value="${currentNode.displayableName}"/>
	<c:if test="${(empty selectableNodeTypes || jcr:isNodeType(currentNode, selectableNodeTypes)) and (empty param.displayablenodeonly or (param.displayablenodeonly eq 'true' and jcr:isDisplayable(currentNode, renderContext)))}">
		<json:property name="classes" value="selectable"/>
    </c:if>
	<json:property name="hasChildren" value="${not empty nodeTypes ? jcr:hasChildrenOfType(currentNode, nodeTypes) : currentNode.nodes.size > 0}"/>
</json:object>
</c:if>
