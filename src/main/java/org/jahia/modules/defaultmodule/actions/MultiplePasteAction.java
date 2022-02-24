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
