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

<h2><fmt:message key="default.manageRoles"/></h2>
<form action="${flowExecutionUrl}" method="POST" id="roleForm">
    <input id="event" type="hidden" name="_event" value="on">
    <input id="role" type="hidden" name="role" />
    <input id="principal" type="hidden" name="principal" />

    <c:forEach items="${roles}" var="entry" >

        <div class="box-1">
            <fieldset>
                <h3> ${entry.key.displayableName} </h3>

                <table class="table table-bordered table-striped table-hover">
                    <thead>
                    <th width="3%">#</th>
                    <th width="16px">&nbsp;</th>
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
                                <img src="<c:url value='/modules/default/css/img/${principalIcon}.png'/>" alt="${principalType}"/>
                            </td>
                            <td>
                                    ${fn:escapeXml(user:displayName(member))}
                            </td>
                            <td>
                                    ${fn:escapeXml(user:fullName(member))}
                            </td>

                            <td>
                                <button style="margin-bottom:0;" class="btn btn-danger btn-small revokeRoleButton"
                                        id="${fn:escapeXml(functions:escapeJavaScript(principalKey))}_${entry.key.name}">
                                    <i class="icon-remove icon-white"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <button class="btn btn-primary grantRoleButton" id="${entry.key.name}">
                    <i class="icon-plus icon-white"></i>
                    <fmt:message key="label.members"/>
                </button>
            </fieldset>

        </div>
    </c:forEach>
</form>
