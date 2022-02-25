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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class CheckClipboardAction extends Action {
    public static final String UUIDS = "uuids[]";

    @SuppressWarnings("unchecked")
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = (List<String>) req.getSession().getAttribute(MultipleCopyAction.UUIDS_TO_COPY);
        List<String> uuidstoCut = (List<String>) req.getSession().getAttribute(MultipleCutAction.UUIDS_TO_CUT);
        if (uuids == null) {
            uuids = uuidstoCut;
        } else if (uuidstoCut != null) {
            uuids.addAll(uuidstoCut);
        }
        if (uuids != null && uuids.size() > 0) {
            JSONObject json = new JSONObject();
            json.put(URLEncoder.encode(UUIDS,"UTF-8"), uuids);
            List<String> paths = new LinkedList<String>();
            List<String> nodetypes = new LinkedList<String>();
            for (String uuid : uuids) {
                try {
                    JCRNodeWrapper nodeByUUID = session.getNodeByUUID(uuid);
                    paths.add(nodeByUUID.getPath());
                    List<String> nodeTypes = nodeByUUID.getNodeTypes();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String nodeType : nodeTypes) {
                        if(stringBuilder.length()>0) {
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append(nodeType);
                    }
                    nodetypes.add(stringBuilder.toString());
                } catch (RepositoryException e) {
                }
            }
            json.put("paths",paths);
            json.put("nodetypes",nodetypes);
            json.put("size", uuids.size());
            return new ActionResult(HttpServletResponse.SC_OK, null, json);
        } else {
            return ActionResult.OK;
        }
    }
}
