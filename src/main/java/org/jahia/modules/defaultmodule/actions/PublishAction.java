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

import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationJob;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Render action for publishing content from default to live workspace.
 * @author loom
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class PublishAction extends Action {

    private JCRPublicationService publicationService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        Set<String> languages = null;
        if (session.getLocale() != null) {
            languages = Collections.singleton(session.getLocale().toString());
        }
        boolean withSubTree = true;
        if (parameters.get("withSubTree") != null) {
            String subTreeStr = parameters.get("withSubTree").get(0);
            withSubTree = Boolean.parseBoolean(subTreeStr);
        }
        boolean immediate = false;
        if (parameters.get("immediate") != null) {
            String immediateStr = parameters.get("immediate").get(0);
            immediate = Boolean.parseBoolean(immediateStr);
        }
        if (immediate) {
            publicationService.publishByMainId(resource.getNode().getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, withSubTree, new ArrayList<String>());
        } else {
            JobDetail jobDetail = BackgroundJob.createJahiaJob("Publication", PublicationJob.class);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            List<String> uuidList = new ArrayList<String>(1);
            uuidList.add(resource.getNode().getIdentifier());
            jobDataMap.put(BackgroundJob.JOB_USERKEY, renderContext.getUser().getUserKey());
            jobDataMap.put(BackgroundJob.JOB_SITEKEY, renderContext.getSite().getSiteKey());
            jobDataMap.put(PublicationJob.PUBLICATION_UUIDS, uuidList);
            jobDataMap.put(PublicationJob.SOURCE, Constants.EDIT_WORKSPACE);
            jobDataMap.put(PublicationJob.DESTINATION, Constants.LIVE_WORKSPACE);
            jobDataMap.put(PublicationJob.CHECK_PERMISSIONS, true);

            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);

        }
        return ActionResult.OK_JSON;
    }
}
