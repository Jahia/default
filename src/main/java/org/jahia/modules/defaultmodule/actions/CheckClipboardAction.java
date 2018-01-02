/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2010
 */
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
