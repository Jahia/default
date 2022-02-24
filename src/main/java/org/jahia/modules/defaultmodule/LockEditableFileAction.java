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
package org.jahia.modules.defaultmodule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.utils.i18n.Messages;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Check the last modified date and lock if the file has not been changed
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class LockEditableFileAction extends LockAction {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String lastModifiedLoaded = req.getParameter("lastModifiedLoaded");
        Map<String,String> res = new HashMap<>();
        if (!resource.getNode().hasProperty(Constants.JCR_LASTMODIFIED) || StringUtils.equals(lastModifiedLoaded, String.valueOf(resource.getNode().getProperty(Constants.JCR_LASTMODIFIED).getValue().getDate().getTimeInMillis()))) {
            return super.doExecute(req, renderContext, resource, session, parameters, urlResolver) ;
        } else {
            res.put("error", Messages.get(resource.getNode().getResolveSite().getTemplatePackage(), "jnt_editableFile.must.reload", renderContext.getUILocale()));
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(res));
        }
    }
}
