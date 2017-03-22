/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.defaultmodule.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Render action for adding specified principal to a group.
 * @author loom
 * Date: 17.02.11
 * Time: 09:26
 */
public class AddMemberToGroupAction extends Action {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddMemberToGroupAction.class);

    private JahiaGroupManagerService jahiaGroupManagerService;
    private JahiaUserManagerService jahiaUserManagerService;

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String groupPath = resource.getNode().getPath();
        String[] splitGroupPath = groupPath.split("/");
        String siteKey = null;
        String groupName = null;
        // path to site group is something like /sites/siteKey/groups/groupName
        if (groupPath.startsWith("/sites")) {
            siteKey = splitGroupPath[2];
            groupName = splitGroupPath[4];
        } else {
            // path to general group is /groups/groupName
            groupName = splitGroupPath[2];
        }
        JCRGroupNode targetJahiaGroup = jahiaGroupManagerService.lookupGroup(siteKey, groupName, session);

        if (parameters.get("userKey") != null) {
            String userKey = parameters.get("userKey").get(0);
            JCRUserNode jahiaUser = jahiaUserManagerService.lookupUserByPath(userKey);
            if (jahiaUser == null) {
                logger.warn("User " + userKey + " could not be found, will not add as member of group " + targetJahiaGroup.getPath());
                return ActionResult.BAD_REQUEST;
            }
            if (!targetJahiaGroup.isMember(jahiaUser)) {
                targetJahiaGroup.addMember(jahiaUser);
            }
        } else if (parameters.get("groupKey") != null) {
            String groupKey = parameters.get("groupKey").get(0);
            JCRGroupNode jahiaGroup = jahiaGroupManagerService.lookupGroupByPath(groupKey);
            if (jahiaGroup == null) {
                logger.warn("Group " + groupKey + " could not be found, will not add as member of group " + targetJahiaGroup.getPath());
                return ActionResult.BAD_REQUEST;
            }
            if (!targetJahiaGroup.isMember(jahiaGroup)) {
                targetJahiaGroup.addMember(jahiaGroup);
            }
        } else {
            return ActionResult.BAD_REQUEST;
        }
        session.save();
        return ActionResult.OK_JSON;
    }
}
