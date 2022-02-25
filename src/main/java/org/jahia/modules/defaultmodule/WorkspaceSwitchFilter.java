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

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.RenderChain;

import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter that switches to another workspace
 */
public class WorkspaceSwitchFilter extends AbstractFilter {

    private static final String SKIP_IN_FILTER ="skipAggregationWorkspaceSwitchFilter";

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String newWorkspace = resource.getNode().getProperty("workspace").getString();
        final HttpServletRequest request = renderContext.getRequest();
        if (!newWorkspace.equals(resource.getWorkspace())) {
            JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(newWorkspace, resource.getNode().getSession().getLocale(), resource.getNode().getSession().getFallbackLocale());
            try {
                JCRNodeWrapper n = s.getNode(resource.getNode().getPath());
                chain.pushAttribute(request, "previousWorkspace", resource.getWorkspace());
                renderContext.setWorkspace(newWorkspace);
                resource.setNode(n);
                renderContext.getMainResource().setNode(s.getNode(renderContext.getMainResource().getNode().getPath()));
                request.setAttribute("workspace", newWorkspace);
                request.setAttribute("currentNode", n);
                if(!AggregateFilter.skipAggregation(request)) {
                    resource.getModuleParams().put(SKIP_IN_FILTER, true);
                    request.setAttribute(AggregateFilter.SKIP_AGGREGATION, true);
                }
            } catch (PathNotFoundException e) {
                return "";
            }
        }
        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final HttpServletRequest request = renderContext.getRequest();
        String previousWorkspace = (String) request.getAttribute("previousWorkspace");
        if (previousWorkspace != null) {
            renderContext.setWorkspace(previousWorkspace);
            JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(previousWorkspace, resource.getNode().getSession().getLocale(), resource.getNode().getSession().getFallbackLocale());
            JCRNodeWrapper n = s.getNode(resource.getNode().getPath());
            resource.setNode(n);
            renderContext.getMainResource().setNode(s.getNode(renderContext.getMainResource().getNode().getPath()));
            request.setAttribute("workspace", previousWorkspace);
            request.setAttribute("currentNode", n);
            if (resource.getModuleParams().get(SKIP_IN_FILTER) != null) {
                request.removeAttribute(AggregateFilter.SKIP_AGGREGATION);
                resource.getModuleParams().remove(SKIP_IN_FILTER);
            }
        }
        return previousOut;
    }
}
