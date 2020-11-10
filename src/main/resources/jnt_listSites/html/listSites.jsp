<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="listsites.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="managesites.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<template:addResources type="css" resources="listsites.css"/>
<template:include view="hidden.header"/>

<c:set var="currentLocale">${currentResource.locale}</c:set>
<c:url var="stagingExportUrl" value="${renderContext.request.contextPath}/cms/export/default/sites_staging_export_${now}.zip"/>
<c:url var="exportUrl" value="${renderContext.request.contextPath}/cms/export/default/sites_export_${now}.zip"/>

<script type="text/javascript">
    $(document).ready(function() {
        $("a.changePropertiesButton").fancybox();
        $("a.detailsButton").fancybox({
            margin : 50,
            scrolling : 'auto',
            width : 600,
            height : 400,
            autoDimensions : false,
            type : 'ajax'
        });
        $(".checkAll").click(function () {
            $(".sitecheckbox").each(function (index) {
                if ($(".checkAll").attr("checked") === "checked") {
                    $(this).attr("checked","checked");
                } else {
                    $(this).removeAttr("checked");
                }
            });
        });

        var db = document.getElementById("deleteSiteButton");
        if (db) {
            db.addEventListener("click", function () {
                deleteSite();
            });
        }

        var esb = document.getElementById("exportStagingButton");
        if (esb) {
            esb.addEventListener("click", function () {
                exportSite('${stagingExportUrl}',false);
            });
        }

        var elb = document.getElementById("exportLiveButton");
        if (elb) {
            elb.addEventListener("click", function () {
                exportSite('${exportUrl}',true);
            });
        }

        var eps = document.getElementsByClassName("editProps");
        for (var i = 0; i < eps.length; i++) {
            eps[i].addEventListener("click", function(e) {
                editProperties(e.currentTarget.id.replace("editProps_", ""));
            });
        }
    });
</script>

<jcr:node var="root" path="/"/>
<c:if test="${moduleMap.end > 0 and moduleMap.end > moduleMap.begin}">
    <c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <button class="deleteSiteButton" id="deleteSiteButton"><fmt:message key="label.manageSite.deleteSite"/></button>
    </c:if>
    <c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <button class="exportStagingButton" id="exportStagingButton"><fmt:message key="label.manageSite.exportStaging"/></button>
        <button class="exportLiveButton" id="exportLiveButton"><fmt:message key="label.manageSite.exportLive"/></button>
    </c:if>
</c:if>

<ul class="list-sites">
<c:forEach items="${moduleMap.currentList}" var="node" begin="${moduleMap.begin}" end="${moduleMap.end}">
    <li class="listsiteicon">
        <c:if test="${(currentNode.properties.export.boolean or currentNode.properties.delete.boolean) and jcr:hasPermission(node,'adminVirtualSites')}">
            <input class="sitecheckbox" type="checkbox" name="${node.name}" />
        </c:if>
        ${node.displayableName}
        <c:choose>
            <c:when test="${currentNode.properties.typeOfContent.string eq 'contents'}">
                <c:set var="page" value="/contents"/>
            </c:when>
            <c:when test="${currentNode.properties.typeOfContent.string eq 'files'}">
                <c:set var="page" value="/files"/>
            </c:when>
            <c:otherwise>
                <c:set var="page" value="/${node.home.name}"/>
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${not empty node and (jcr:hasPermission(node,'jContentAccess') || jcr:hasPermission(node,'contributeModeAccess'))}">
                    <c:set var="baseLive" value="${url.baseLive}"/>
                    <c:set var="basePreview" value="${url.basePreview}"/>
                    <c:set var="baseContribute" value="${url.baseContribute}"/>
                    <c:set var="baseEdit" value="${url.baseEdit}"/>
                    <c:set var="siteInLang" value="false"/>
                    <c:set var="currentLocale">${currentResource.locale}</c:set>
                    <c:if test="${currentNode.properties.administrationlink.boolean && jcr:hasPermission(node,'siteAdministrationAccess')}">
                        <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="<c:url value='${baseEdit}${node.path}.manageModules.html'/>"><fmt:message
                            key="label.administration"/></a>
                    </c:if>
                    <c:forEach items="${node.languages}" var="mapLang">
                    	<c:if test="${currentLocale == mapLang}">
                    		<c:set var="siteInLang" value="true"/>
                    	</c:if>
                    </c:forEach>
                    <c:if test="${not siteInLang}">
                        <c:set var="localeLength" value="${fn:length(fn:toUpperCase(currentResource.locale))}"/>
                        <c:set var="baseLive"
                               value="${fn:substring(url.baseLive,-1,fn:length(url.baseLive)-localeLength)}${node.defaultLanguage}"/>
                        <c:set var="basePreview"
                               value="${fn:substring(url.basePreview,-1,fn:length(url.basePreview)-localeLength)}${node.defaultLanguage}"/>
                        <c:set var="baseContribute"
                               value="${fn:substring(url.baseContribute,-1,fn:length(url.baseContribute)-localeLength)}${node.defaultLanguage}"/>
                        <c:set var="baseEdit"
                               value="${fn:substring(url.baseEdit,-1,fn:length(url.baseEdit)-localeLength)}${node.defaultLanguage}"/>
                    </c:if>
                    <c:set var="remotelyPublished" value="${jcr:isNodeType(node,'jmix:remotelyPublished')}"/>
                    <c:if test="${currentNode.properties.edit.boolean && jcr:hasPermission(node,'jContentAccess') && !renderContext.settings.readOnlyMode && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                        <img src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="<c:url value='${baseEdit}${node.path}${page}.html'/>"><fmt:message
                            key="label.editMode"/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.contribute.boolean  && jcr:hasPermission(node,'contributeModeAccess') && !renderContext.settings.readOnlyMode && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                        <c:url value='/icons/contribute.png' var="icon"/>
                        <c:if test="${currentNode.properties.typeOfContent.string eq 'contents'}">
                            <c:url value='/icons/content-manager-1616.png' var="icon"/>
                        </c:if>
                        <img src="${icon}" width="16" height="16" alt=" " role="presentation"
                             style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="<c:url value='${baseContribute}${node.path}${page}.html'/>"><fmt:message
                            key="label.contribute"/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.preview.boolean && jcr:hasPermission(node,'jcr:read_default') && !renderContext.settings.readOnlyMode && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                        <img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="<c:url value='${basePreview}${node.path}${page}.html'/>"><fmt:message
                            key="label.preview"/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.live.boolean && (node.home.properties['j:published'].boolean or remotelyPublished)}">
                        <img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="<c:url value='${baseLive}${node.path}${page}.html'/>"><fmt:message
                            key="label.live"/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.editproperties.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="#editSiteDiv${node.identifier}" class="changePropertiesButton" id="changePropertiesButton${node.identifier}"><fmt:message key="label.manageSite.changeProperties"/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.details.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative; top: 4px; margin-right:2px; "/><a
                            href="<c:url value='${basePreview}${node.path}${page}.${currentNode.properties.detailsTemplate.string}.html'/>"
                            class="detailsButton" id="detailsButton${node.identifier}">${currentNode.properties.detailsLabel.string}</a>
                    </c:if>

                    <jsp:useBean id="nowDate" class="java.util.Date" />
                    <fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>

                    <c:if test="${currentNode.properties.editproperties.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <div style="display:none">
                            <div id="editSiteDiv${node.identifier}" class="popupSize">
                                <form class="editSiteForm ajaxForm" id="editSiteForm${node.identifier}" action="<c:url value='${url.base}${node.path}.adminEditSite.do'/>" method="post" >

                                    <fieldset>
                                        <legend><fmt:message key="label.manageSite.siteProperties"/></legend>
                                        <h3><fmt:message key="label.manageSite.siteProperties"/></h3>

                                        <p id="siteTitleForm${node.identifier}">
                                            <label for="siteTitle${node.identifier}"><fmt:message key="jnt_virtualsite.j_title"/> (*)</label>
                                            <input type="text" name="siteTitle" id="siteTitle${node.identifier}" value="${node.properties['j:title'].string}"/>
                                        </p>

                                        <p id="siteServerNameForm${node.identifier}">
                                            <label for="siteServerName${node.identifier}"><fmt:message key="jnt_virtualsite.j_serverName"/> (*)</label>
                                            <input type="text" name="siteServerName" id="siteServerName${node.identifier}" value="${node.properties['j:serverName'].string}"/>
                                        </p>

                                        <p id="siteDescrForm${node.identifier}">
                                            <label for="siteDescr${node.identifier}"><fmt:message key="jnt_virtualsite.j_description"/></label>
                                            <textarea type="text" name="siteDescr" id="siteDescr${node.identifier}">${node.properties['j:description'].string}</textarea>
                                        </p>
                                    </fieldset>
                                </form>
                                <button site="${node.identifier}" class="editProps" id="editProps_${node.identifier}"><fmt:message key="label.manageSite.submitChanges"/></button>
                            </div>
                        </div>
                    </c:if>
            </c:when>
            <c:otherwise>
                <c:set var="jContentAccessNode"
                       value="${jcr:getFirstAllowedNodeForPermission('jContentAccess', node, 'jnt:page')}"/>
                <c:set var="contributeModeAccessNode"
                       value="${jcr:getFirstAllowedNodeForPermission('contributeModeAccess', node, 'jnt:page')}"/>
                <c:set var="previewModeAccessNode"
                       value="${jcr:getFirstAllowedNodeForPermission('jcr:read_default', node, 'jnt:page')}"/>
                <c:if test="${node.home.properties['j:published'].boolean or not empty jContentAccessNode or not empty contributeModeAccessNode or not empty previewModeAccessNode}">
                        <c:set var="baseLive" value="${url.baseLive}"/>
                        <c:set var="basePreview" value="${url.basePreview}"/>
                        <c:set var="baseContribute" value="${url.baseContribute}"/>
                        <c:set var="baseEdit" value="${url.baseEdit}"/>
                        <c:if test="${not functions:contains(node.languages, currentLocale)}">
                            <c:set var="localeLength" value="${fn:length(currentLocale)}"/>
                            <c:set var="baseLive"
                                   value="${fn:substring(url.baseLive,-1,fn:length(url.baseLive)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="basePreview"
                                   value="${fn:substring(url.basePreview,-1,fn:length(url.basePreview)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="baseContribute"
                                   value="${fn:substring(url.baseContribute,-1,fn:length(url.baseContribute)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="baseEdit"
                                   value="${fn:substring(url.baseEdit,-1,fn:length(url.baseEdit)-localeLength)}${node.defaultLanguage}"/>
                        </c:if>

                        <c:if test="${not empty jContentAccessNode && currentNode.properties.contribute.boolean && !renderContext.settings.readOnlyMode && !renderContext.settings.distantPublicationServerMode}">
                            <img src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseEdit}${jContentAccessNode.path}.html'/>"><fmt:message
                                key="label.editMode"/></a>
                        </c:if>

                        <c:if test="${not empty contributeModeAccessNode && currentNode.properties.contribute.boolean && !renderContext.settings.readOnlyMode && !renderContext.settings.distantPublicationServerMode}">
                            <img src="<c:url value='/icons/contribute.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseContribute}${contributeModeAccessNode.path}.html'/>"><fmt:message
                                key="label.contribute"/></a>
                        </c:if>

                        <c:if test="${not empty previewModeAccessNode && currentNode.properties.preview.boolean && !renderContext.settings.readOnlyMode && !renderContext.settings.distantPublicationServerMode}">
                            <img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${basePreview}${previewModeAccessNode.path}.html'/>"><fmt:message
                                key="label.preview"/></a>
                        </c:if>
                        <c:if test="${currentNode.properties.live.boolean && node.home.properties['j:published'].boolean}">
                            <img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseLive}${node.path}${page}.html'/>"><fmt:message
                                key="label.live"/></a>
                        </c:if>
                </c:if>
            </c:otherwise>
        </c:choose>
        </li>
    </c:forEach>
</ul>

<div style="display:none">
    <div id="dialog-delete-confirm" title=" ">
        <p><span class="ui-icon ui-icon-alert"
                 style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.delete.confirm" /></p>
    </div>
    <div id="nothing-selected" >
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.manageSites.noSiteSelected"/></p>
    </div>
</div>
<div style="display:none; position:fixed; left:0; top:0; width:100%; height:100%; z-index:9999" class="loading">
    <h1><fmt:message key="label.workInProgressTitle"/></h1>
</div>

<c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
    <form class="deleteSiteForm ajaxForm" id="deleteSiteForm" action="<c:url value='${url.base}/sites.adminDeleteSite.do'/>" method="post" >
    </form>
</c:if>
<c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
    <form class="exportForm ajaxForm"  name="export" id="exportForm" method="POST">
        <input type="hidden" name="exportformat" value="site"/>
        <input type="hidden" name="live" value="true"/>
    </form>
</c:if>

<template:include view="hidden.footer"/>
