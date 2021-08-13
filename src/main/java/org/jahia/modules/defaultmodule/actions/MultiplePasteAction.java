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
package org.jahia.modules.defaultmodule.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class MultiplePasteAction extends Action {
    private transient static Logger logger = LoggerFactory.getLogger(MultiplePasteAction.class);

    @SuppressWarnings("unchecked")
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = (List<String>) req.getSession().getAttribute(MultipleCopyAction.UUIDS_TO_COPY);
        if (uuids != null && uuids.size() > 0) {
            JCRNodeWrapper targetNode = resource.getNode();
            String targetPath = targetNode.getPath();
            try {
                for (String uuid : uuids) {
                    JCRNodeWrapper node = session.getNodeByUUID(uuid);
                    if (targetPath.startsWith(node.getPath())) {
                        // do not copy recursively
                        continue;
                    }
                    session.checkout(node);
                    node.copy(targetNode, JCRContentUtils.findAvailableNodeName(targetNode, node.getName()), true, null, SettingsBean.getInstance().getImportMaxBatch());
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return ActionResult.BAD_REQUEST;
            }
        }
        req.getSession().removeAttribute(MultipleCopyAction.UUIDS_TO_COPY);
        uuids = (List<String>) req.getSession().getAttribute(MultipleCutAction.UUIDS_TO_CUT);
        if (uuids != null && uuids.size() > 0) {
            JCRNodeWrapper targetNode = resource.getNode();
            String targetPath = targetNode.getPath();
            try {
                for (String uuid : uuids) {
                    JCRNodeWrapper node = session.getNodeByUUID(uuid);
                    if (targetPath.startsWith(node.getPath())) {
                        // do not move recursively
                        continue;
                    }
                    session.checkout(node);
                    session.move(node.getPath(),targetNode.getPath()+"/"+JCRContentUtils.findAvailableNodeName(targetNode, node.getName()));
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return ActionResult.BAD_REQUEST;
            }
        }
        req.getSession().removeAttribute(MultipleCutAction.UUIDS_TO_CUT);
        return ActionResult.OK_JSON;
    }
}
