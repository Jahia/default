/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
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
 */
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
