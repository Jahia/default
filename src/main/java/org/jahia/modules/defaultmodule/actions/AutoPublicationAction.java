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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Allows enabling/disabling the auto-publication on the node.
 *
 * @author Sergiy Shyrkov
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class AutoPublicationAction extends Action {

    private static final Logger logger = LoggerFactory.getLogger(AutoPublicationAction.class);

    private JCRPublicationService publicationService;

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
            Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters,
            URLResolver urlResolver) throws Exception {

        boolean enable = Boolean.valueOf(getParameter(parameters, "enable", "true"));

        boolean unpublish = !enable
                && Boolean.valueOf(getParameter(parameters, "unpublish", "true"));

        JCRNodeWrapper node = resource.getNode();

        if (enable) {
            if (!node.isNodeType("jmix:autoPublish")) {
                session.checkout(node);
                node.addMixin("jmix:autoPublish");
                session.save();
                if (logger.isDebugEnabled()) {
                    logger.debug("Made node {} auto-published", node.getPath());
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Node {} is already auto-published", node.getPath());
            }
        } else {
            if (node.isNodeType("jmix:autoPublish")) {
                session.checkout(node);
                node.removeMixin("jmix:autoPublish");
                session.save();
                if (logger.isDebugEnabled()) {
                    logger.debug("Reverted auto-publishing on the node {}", node.getPath());
                }
            }
        }

        if (unpublish) {
            publicationService.unpublish(ImmutableList.of(node.getIdentifier()));
            if (logger.isDebugEnabled()) {
                logger.debug("Unpublished node {}", node.getPath());
            }
        }

        return ActionResult.OK_JSON;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

}
