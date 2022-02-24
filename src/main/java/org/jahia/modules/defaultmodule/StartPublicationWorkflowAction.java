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
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class StartPublicationWorkflowAction extends StartWorkflowAction {
    private JCRPublicationService publicationService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String process = parameters.get("process").get(0);
        String workflowDefinitionKey = StringUtils.substringAfter(process, ":");
        String providerKey = StringUtils.substringBefore(process, ":");

        String formNodeType = workflowService.getWorkflowRegistration(workflowDefinitionKey).getForms().get("start");
        Map<String, Object> map = WorkflowVariable.getVariablesMap(parameters, formNodeType, Arrays.asList("process"));
        final LinkedHashSet<String> languages = new LinkedHashSet<>();
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
