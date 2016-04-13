package org.jahia.modules.defaultmodule.actions;


import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.services.content.*;
import org.jahia.bin.ActionResult;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
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
        // Apply on can be jnt:globalSetting , .....
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
        String[] types = new String[]{applyOn};
        contentTemplateNode.setProperty("j:applyOn", types);

        base.getSession().save();
        return ActionResult.OK;
    }
}
