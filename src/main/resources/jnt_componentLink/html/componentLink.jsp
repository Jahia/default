<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:url var="targetUrl" value='${url.base}${boundComponent.path}.${currentNode.properties["targetTemplate"].string}.html'/>

<div class="${fn:escapeXml(currentNode.properties['divClass'].string)}"><!--start preferences-->
    <a title="${fn:escapeXml(currentNode.displayableName)}" class="${fn:escapeXml(currentNode.properties['aClass'].string)}" href="${fn:escapeXml(targetUrl)}">${fn:escapeXml(currentNode.displayableName)}</a>
</div>