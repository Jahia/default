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

import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.helper.PublicationHelper;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;
import org.jahia.utils.i18n.Messages;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.*;

/**
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class MultiplePublishAction extends Action {
    private WorkflowService workflowService;
    private PublicationHelper publicationHelper;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPublicationHelper(PublicationHelper publicationHelper) {
        this.publicationHelper = publicationHelper;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = parameters.get(MultipleCopyAction.UUIDS);

        Set<String> locales = new LinkedHashSet<String>(Arrays.asList(
                renderContext.getMainResourceLocale().toString()));

        List<GWTJahiaPublicationInfo> pubInfos = publicationHelper.getFullPublicationInfos(uuids, locales, session, false,
                false);

        if (pubInfos.size() == 0) {
            return ActionResult.BAD_REQUEST;
        }

        Map<PublicationWorkflow, WorkflowDefinition> workflows = publicationHelper.createPublicationWorkflows(pubInfos);

        for (Map.Entry<PublicationWorkflow, WorkflowDefinition> entry : workflows.entrySet()) {
            final HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("customWorkflowInfo", entry.getKey());

            String title = Messages.getInternalWithArguments("label.workflow.start.message", session.getLocale(), "{0} started by {1} on {2} - {3} content items involved",
                    entry.getValue().getDisplayName(), session.getUser().getName(), DateFormat.getDateInstance(DateFormat.SHORT, session.getLocale()).format(new Date()), pubInfos.size());

            WorkflowVariable var = new WorkflowVariable(title, PropertyType.STRING);
            map.put("jcr:title", var);
            
            if (entry.getValue() != null) {
                workflowService.startProcessAsJob(entry.getKey().getAllUuids(),
                        session, entry.getValue().getKey(),
                        entry.getValue().getProvider(), map, null);
            }
        }
        return ActionResult.OK_JSON;
    }
}
