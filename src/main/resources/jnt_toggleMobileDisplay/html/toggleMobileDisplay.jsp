<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ page import="org.jahia.services.channels.Channel" %>
<%@ page import="org.jahia.services.channels.filters.ChannelResolutionFilter" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="cookieName" value="org.jahia.channels.activeChannel" />
<c:if test="${not empty renderContext.channel and (not renderContext.channel.generic or not empty cookie[cookieName])}">
    <template:addResources type="javascript" resources="apps/default.toggleMobileDisplay.bundle.js"/>
    <script type="text/javascript">
        toggleMobileDisplayLib.initCookieHandler('${cookieName}', '${empty cookie[cookieName]}');
    </script>

    <c:choose>
        <c:when test="${empty cookie[cookieName]}">
            <a href="#" id="forceGenericChannel"><fmt:message key="label.channels.force.generic" /></a>
        </c:when>
        <c:otherwise>
            <a href="#" id="useCurrentChannel"><fmt:message key="label.channels.use.current" /></a>
        </c:otherwise>
    </c:choose>

</c:if>
