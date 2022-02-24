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
import org.jahia.services.content.*;
import org.jahia.bin.ActionResult;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 3 juin 2010
 */
public class AddComment extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = resource.getNode();
        if (!node.isNodeType("jmix:comments")) {
            node.addMixin("jmix:comments");
            session.save();
        }

        final JCRNodeWrapper[] newNode = {null};
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), org.jahia.api.Constants.LIVE_WORKSPACE, null, jcrSessionWrapper -> {
            String path = node.getPath() + "/comments";
            newNode[0] = createNode(req, parameters, jcrSessionWrapper.getNode(path), "jnt:post", "", false);
            jcrSessionWrapper.save();

            return null;
        });

        return new ActionResult(HttpServletResponse.SC_OK, newNode[0].getPath());
    }
}
