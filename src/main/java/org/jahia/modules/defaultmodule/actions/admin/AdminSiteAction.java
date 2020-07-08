/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.defaultmodule.actions.admin;

import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Base class for site admin action
 * @deprecated Sample action, will be removed
 */
@Deprecated
public abstract class AdminSiteAction extends AdminAction {

    protected JahiaSitesService sitesService;

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> sites = parameters.get("sitebox");
        if (sites == null) {
            JCRNodeWrapper node = resource.getNode();

            if (!node.isNodeType("jnt:virtualsite")  || !node.getParent().getPath().equals("/sites")) {
                return ActionResult.BAD_REQUEST;
            }

            JahiaSite site = sitesService.getSiteByKey(node.getName(), session);

            return doExecute(req, renderContext, site, session, parameters);
        } else {
            JCRNodeWrapper node = resource.getNode();

            if (!node.isNodeType("jnt:virtualsitesFolder")  || !node.getPath().equals("/sites")) {
                return ActionResult.BAD_REQUEST;
            }

            for (String site : sites) {
                doExecute(req,renderContext, sitesService.getSiteByKey(site), session, parameters);
            }
        }
        return ActionResult.OK_JSON;
    }

    public abstract ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, JahiaSite site, JCRSessionWrapper session, Map<String, List<String>> parameters) throws Exception;
}
