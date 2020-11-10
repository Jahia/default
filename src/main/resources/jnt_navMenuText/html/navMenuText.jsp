<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<a href="#" style="cursor:default;">${fn:escapeXml(currentNode.properties["jcr:title"].string)}</a>
