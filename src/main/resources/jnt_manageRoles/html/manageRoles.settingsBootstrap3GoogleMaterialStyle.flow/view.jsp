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

    $(document).ready(function() {
        var grants = document.getElementsByClassName("grantRoleButton");
        for (var i = 0; i < grants.length; i++) {
            grants[i].addEventListener("click", function(e) {
                grantRole(e.currentTarget.id);
            });
        }

        var revokes = document.getElementsByClassName("revokeRoleButton");
        for (var i = 0; i < revokes.length; i++) {
            revokes[i].addEventListener("click", function(e) {
                var split = e.currentTarget.id.split("_");
                revokeRole(split[0], split[1]);
            });
        }
    });
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
            <div class="panel-heading">
                <div class="row">
                    <div class="col-md-6">
                        <h4>${entry.key.displayableName}</h4>
                    </div>
                    <div class="col-md-6">
                        <button class="btn btn-primary btn-raised pull-right grantRoleButton" data-sel-role="grantRole-${entry.key.name}"
                                id="${entry.key.name}">
                            <fmt:message key='label.edit' />
                        </button>
                    </div>
                </div>
            </div>

            <div class="panel-body">
                <table class="table table-bordered table-striped" data-sel-role="${entry.key.name}-users">
                    <thead>
                    <th width="3%">#</th>
                    <th width="16px">&nbsp;</th>
                    <th width="25%"><fmt:message key="label.username"/></th>
                    <th><fmt:message key="label.name"/></th>
                    <th width="15%"><fmt:message key="label.actions"/></th>
                    </thead>
                    <tbody>
                    <c:forEach items="${entry.value}" var="member" varStatus="loopStatus">
                        <c:set var="principalType" value="${jcr:isNodeType(member,'jnt:user')?'u':'g'}"/>
                        <c:set var="principalIcon" value="${principalType == 'u' ? 'person' : 'people'}"/>
                        <c:set var="principalKey" value="${principalType}:${member.name}"/>
                        <tr>
                            <td>
                                    ${loopStatus.count}
                            </td>
                            <td>
                                <i class="material-icons" style="vertical-align:middle">${principalIcon}</i>
                            </td>
                            <td>
                                    ${fn:escapeXml(user:displayName(member))}
                            </td>
                            <td>
                                    ${fn:escapeXml(user:fullName(member))}
                            </td>

                            <td>
                                <a class="btn btn-fab btn-fab-xs btn-danger revokeRoleButton" title="<fmt:message key='label.delete'/>" href="#delete" id="${fn:escapeXml(principalKey)}_${entry.key.name}">
                                    <i class="material-icons">delete</i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>
    </c:forEach>
</form>
