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
package org.jahia.modules.defaultmodule;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author toto
 * Date: Mar 18, 2010
 * Time: 12:16:14 PM
 */
public class StartPublicationWorkflowAction extends StartWorkflowAction {
    private JCRPublicationService publicationService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String process = parameters.get("process").get(0);
        String workflowDefinitionKey = StringUtils.substringAfter(process, ":");
        String providerKey = StringUtils.substringBefore(process, ":");

        String formNodeType = workflowService.getWorkflowRegistration(workflowDefinitionKey).getForms().get("start");
        Map<String, Object> map = WorkflowVariable.getVariablesMap(parameters, formNodeType, Arrays.asList("process"));
        final LinkedHashSet<String> languages = new LinkedHashSet<String>();
        languages.add(resource.getLocale().toString());
        final List<PublicationInfo> infoList = publicationService.getPublicationInfo(resource.getNode().getIdentifier(),
                                                                                     languages, true, true, false,
                                                                                     resource.getNode().getSession().getWorkspace().getName(),
                                                                                     "live");
        map.put("publicationInfos", infoList);
        workflowService.startProcessAsJob(Arrays.asList(resource.getNode().getIdentifier()), session, workflowDefinitionKey, providerKey, map, null);
        return ActionResult.OK_JSON;
    }

}
