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
