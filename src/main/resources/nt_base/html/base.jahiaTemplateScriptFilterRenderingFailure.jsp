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
<%--@elvariable id="errorId" type="java.lang.String"--%>

<c:if test="${renderContext.editMode}">
    <p>
        <fmt:message key="renderFailure.errorOccurredInView"/>&nbsp;<strong data-sel-role="renderingFailureViewKey">${not empty originalViewKey ? fn:escapeXml(originalViewKey) : ''}</strong>.<br>
        <fmt:message key="renderFailure.viewPath"/>&nbsp;<strong data-sel-role="renderingFailureViewPath">${not empty originalViewPath ? fn:escapeXml(originalViewPath) : ''}</strong>.<br>
        <c:choose>
            <c:when test="${not empty error}">
                <fmt:message key="renderFailure.errorSays"/>&nbsp;<strong data-sel-role="renderingFailureErrorMessage">${not empty error.message ? fn:escapeXml(error.message) : ''}</strong>.<br>
            </c:when>
            <c:otherwise>
                <fmt:message key="renderFailure.checkServerLogs"/>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty errorId && not empty printedError}">
            <button data-sel-role="renderingFailureToggleFullError" onclick="jahiaRenderingFullErrorToggleStackTrace('${errorId}')"><fmt:message key="renderFailure.toggleFullError"/></button>
        </c:if>
    </p>

    <c:if test="${not empty errorId && not empty printedError}">
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
        <div data-sel-role="renderingFailureFullError" class="jahiaRenderingFullErrorStackTrace_${errorId}" style="display: none">
                ${printedError}
        </div>
    </c:if>
</c:if>