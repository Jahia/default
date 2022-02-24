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



import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.bin.Action;
import org.jahia.services.content.*;
import org.jahia.bin.ActionResult;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.utils.i18n.Messages;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Action to create A board (site settings or server settings, ....)
 * @author achaabni
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class CreateBoardAction extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper jcrSessionWrapper, Map<String, List<String>> map, URLResolver urlResolver) throws Exception {
        // This action should be called only in the studio
        if(!renderContext.getMainResource().getPath().startsWith("/modules"))
        {
            return  ActionResult.BAD_REQUEST;
        }

        JCRNodeWrapper node = resource.getNode();
        //Get Request parameters

        // Board Type can be server-settings-base, site-settings-base
        String boardType = req.getParameter("boardType");
        // Apply on can be jnt:globalSettings , .....
        String applyOn = req.getParameter("applyOn");
        //View for the Board
        String view = req.getParameter("view");
        // Content Template Name to add
        String contentTemplateName = req.getParameter("contentTemplateName");

        //If one of the parameters is absent return a bad request
        if(StringUtils.isEmpty(boardType) || StringUtils.isEmpty(applyOn) || StringUtils.isEmpty(view) || StringUtils.isEmpty(contentTemplateName))
        {
            return  ActionResult.BAD_REQUEST;
        }

        JCRNodeWrapper base = null;
        if(node.hasNode(boardType))
        {
            base = node.getNode(boardType);
        }else {
            base = node.addNode(boardType, "jnt:template");
            base.setProperty("j:view",view);
        }
        JCRNodeWrapper contentTemplateNode = base.addNode(JCRContentUtils.findAvailableNodeName(base,
                contentTemplateName),
                "jnt:contentTemplate");
        String[] types = StringUtils.split(applyOn, " ,");
        contentTemplateNode.setProperty("j:applyOn", types);

        base.getSession().save();

        // Add a parameter to refresh screen after reception
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(Linker.REFRESH_ALL, true);
        jsonObject.put("refreshData", data);
        JSONObject messageDisplay = new JSONObject();
        String defaultJahiaTemplatesBundles = "resources.DefaultJahiaTemplates";
        String boardLabel = getBoardLabel(boardType,renderContext.getUILocale(),defaultJahiaTemplatesBundles);
        messageDisplay.put("title", Messages.getWithArgs(defaultJahiaTemplatesBundles, "label.board.create.successful.title",
                renderContext.getUILocale(), boardLabel));
        messageDisplay.put("text",Messages.getWithArgs(defaultJahiaTemplatesBundles, "label.board.create.successful.message",
                renderContext.getUILocale(), boardLabel));
        messageDisplay.put("messageBoxType","info");
        jsonObject.put("messageDisplay",messageDisplay);

        return new ActionResult(HttpServletResponse.SC_OK, null, jsonObject);

    }

    /**
     * Get a board label
     * @param boardType boardType
     * @param uiLocale locale
     * @param bundle bundle name
     * @return the board label in the specific locale
     */
    private String getBoardLabel(String boardType, Locale uiLocale, String bundle) {
        return Messages.get(bundle, "label.board." + boardType.replaceAll("-", ""),uiLocale);
    }
}
