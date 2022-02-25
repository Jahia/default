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
package org.jahia.modules.defaultmodule;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class LockAction extends Action {

    private static final Logger logger = LoggerFactory.getLogger(LockAction.class);

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String type = req.getParameter("type");
        Map<String,String> res = new HashMap<>();
        try {
            // avoid to lock multiple times the same lock
            if (resource.getNode().hasProperty("j:lockTypes")) {
                for (Value v : resource.getNode().getProperty("j:lockTypes").getValues()) {
                    String owner = StringUtils.substringBefore(v.getString(), ":");
                    String currentType = StringUtils.substringAfter(v.getString(), ":");
                    if (StringUtils.equals(owner,session.getUserID()) && StringUtils.equals(currentType,type)) {
                        // lock already set on this node
                        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(res));
                    }
                }
            }
            resource.getNode().lockAndStoreToken(type);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            res.put("error", e.getMessage());
        }
        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(res));
    }
}
