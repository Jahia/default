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
