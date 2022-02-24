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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * An advanced action that will add the specified principals to the specified roles for the resource specified in the
 * URL. Of course proper permissions must be given to execute this action.
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class AddPrincipalsInRolesAction extends Action {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddPrincipalsInRolesAction.class);

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

        if ((parameters.get("principals") != null) &&
            (parameters.get("roles") != null)) {
            List<String> principals = parameters.get("principals");
            List<String> roles = parameters.get("roles");

            String siteKey = resource.getNode().getResolveSite().getSiteKey();

            for (String principalKey : principals) {
                if (principalKey.startsWith("u:")) {
                    String user = principalKey.substring("u:".length());
                    JCRUserNode jahiaUser = jahiaUserManagerService.lookupUser(user,siteKey);
                    if (jahiaUser == null) {
                        logger.warn("User " + user + " could not be found, will not add to roles");
                        return ActionResult.BAD_REQUEST;
                    }
                    resource.getNode().grantRoles(principalKey, new HashSet<String>(roles));
                    session.save();
                } else if (principalKey.startsWith("g:")) {
                    String group = principalKey.substring("g:".length());
                    JCRGroupNode jahiaGroup = jahiaGroupManagerService.lookupGroup(siteKey, group);
                    if (jahiaGroup == null) {
                        jahiaGroup = jahiaGroupManagerService.lookupGroup(null, group);
                    }
                    if (jahiaGroup == null) {
                        logger.warn("Group " + group + " could not be found, will not add to roles");
                        return ActionResult.BAD_REQUEST;
                    }
                    resource.getNode().grantRoles(principalKey, new HashSet<String>(roles));
                    session.save();
                }
            }
        } else {
            return ActionResult.BAD_REQUEST;
        }
        return ActionResult.OK_JSON;
    }
}
