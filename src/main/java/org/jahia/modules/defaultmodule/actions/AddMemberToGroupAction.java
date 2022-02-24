/*
 * Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * @deprecated Sample action, will be removed
 */
@Deprecated
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
