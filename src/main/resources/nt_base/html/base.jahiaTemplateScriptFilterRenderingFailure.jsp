<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="error" type="java.lang.Exception"--%>
<%--@elvariable id="printedError" type="java.lang.String"--%>
<%--@elvariable id="originalViewKey" type="java.lang.String"--%>
<%--@elvariable id="originalViewPath" type="java.lang.String"--%>

<c:if test="${renderContext.editMode}">
    <template:addResources key="jahiaRenderingFullErrorToggler">
        <script>
            function jahiaRenderingFullErrorToggleStackTrace(uuid) {
                let elements = document.querySelectorAll('.jahiaRenderingFullErrorStackTrace_' + uuid);
                elements.forEach(function(element) {
                    if (element.style.display === "none") {
                        element.style.display = "block";
                    } else {
                        element.style.display = "none";
                    }
                });
            }
        </script>
    </template:addResources>

    <p>
        <fmt:message key="renderFailure.errorOccurredInView"/>&nbsp;<strong>${not empty originalViewKey ? fn:escapeXml(originalViewKey) : ''}</strong>.<br>
        <fmt:message key="renderFailure.viewPath"/>&nbsp;<strong>${not empty originalViewPath ? fn:escapeXml(originalViewPath) : ''}</strong>.<br>
        <fmt:message key="renderFailure.errorSays"/>&nbsp;<strong>${not empty error && not empty error.message ? fn:escapeXml(error.message) : ''}</strong>.<br>
        <button onclick="jahiaRenderingFullErrorToggleStackTrace('${currentNode.identifier}')"><fmt:message key="renderFailure.toggleFullError"/></button>
    </p>
    <div class="jahiaRenderingFullErrorStackTrace_${currentNode.identifier}" style="display: none">
        ${printedError}
    </div>
</c:if>