<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<script type="text/javascript">

    function grantRole(role) {
        $('#role').val(role);
        $('#event').attr('name','_eventId_editRoleMembers');
        $('#roleForm').submit();
    }

    function revokeRole(principal,role) {
        if(confirm('<fmt:message key="default.manageRoles.revokeRole.confirm" />')) {
            $('#role').val(role);
            $('#principal').val(encodeURIComponent(principal));
            $('#event').attr('name','_eventId_revokeRole');
            $('#roleForm').submit();
        }
    }
</script>

<div class="page-header">
    <h2>
        <fmt:message key="default.manageRoles"/>
    </h2>
</div>

<form action="${flowExecutionUrl}" method="POST" id="roleForm">
    <input id="event" type="hidden" name="_event" value="on">
    <input id="role" type="hidden" name="role" />
    <input id="principal" type="hidden" name="principal" />

    <c:forEach items="${roles}" var="entry" >

        <div class="panel panel-default">
            <fieldset>
                <div class="panel-heading">
                    ${entry.key.displayableName}
                </div>
                
                <div class="panel-body">
                    <table class="table table-bordered table-striped table-hover">
                        <thead>
                        <th width="3%">#</th>
                        <th width="25%"><fmt:message key="label.name"/></th>
                        <th><fmt:message key="label.properties"/></th>
                        <th width="15%"><fmt:message key="label.actions"/></th>
                        </thead>
                        <tbody>
                        <c:forEach items="${entry.value}" var="member" varStatus="loopStatus">
                            <c:set var="principalType" value="${jcr:isNodeType(member,'jnt:user')?'u':'g'}"/>
                            <c:set var="principalIcon" value="${principalType == 'u' ? 'icon-user-small' : 'icon-group-small'}"/>
                            <c:set var="principalKey" value="${principalType}:${member.name}"/>
                            <tr>
                                <td>
                                        ${loopStatus.count}
                                </td>
                                <td>
                                        ${fn:escapeXml(user:displayName(member))}
                                </td>
                                <td>
                                        ${fn:escapeXml(user:fullName(member))}
                                </td>
    
                                <td>
                                    <a class="btn btn-fab btn-fab-xs btn-danger" title="<fmt:message key='label.delete'/>" href="#delete" onclick="revokeRole('${fn:escapeXml(functions:escapeJavaScript(principalKey))}','${entry.key.name}')">
                                        <i class="material-icons">delete</i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <span class="input-group-btn">
                        <button class="btn btn-sm btn-primary" onclick="grantRole('${entry.key.name}')">
                            <fmt:message key='label.members' />
                        </button>
                    </span>
                </div>
            </fieldset>

        </div>
    </c:forEach>
</form>
