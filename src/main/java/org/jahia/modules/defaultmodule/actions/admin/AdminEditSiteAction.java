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
package org.jahia.modules.defaultmodule.actions.admin;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.ActionResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Edit site properties
 * @deprecated Sample action, will be removed
 */
@Deprecated
public class AdminEditSiteAction extends AdminSiteAction {
    private static Logger logger = LoggerFactory.getLogger(AdminEditSiteAction.class);

    @Override
    public String getMessage(Locale locale, String key) {
        String message = Messages.get("resources.JahiaServerSettings", key, locale);
        return StringUtils.isEmpty(message)?super.getMessage(locale, key):message;
    }


    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, JahiaSite site, JCRSessionWrapper session, Map<String, List<String>> parameters) throws Exception {
        logger.debug(" process edit site started ");

        // get form values...
        String siteTitle = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteTitle")).trim(), 100);
        String siteServerName = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteServerName")).trim(), 200);
        String siteDescr = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteDescr")).trim(), 250);

        boolean defaultSite = (getParameter(parameters, "defaultSite") != null);

        Map<String, String> result = new HashMap<String, String>();

        try {
            session.checkout((Node) site);
            // check validity...
            if (siteTitle != null && (siteTitle.trim().length() > 0) && siteServerName != null &&
                    (siteServerName.trim().length() > 0)) {
                if (!sitesService.isServerNameValid(siteServerName)) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.invalidServerName"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (!site.getServerName().equals(siteServerName)) {
                    if (!Url.isLocalhost(siteServerName) && sitesService.getSite(siteServerName) != null) {
                        result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName"));
                        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                    }
                }
            } else {
                result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.completeRequestInfo"));
                return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
            }

            // save modified informations...
            site.setTitle(siteTitle);
            site.setServerName(siteServerName);
            site.setDescription(siteDescr);

            sitesService.updateSystemSitePermissions(site);

            JahiaSite defSite = sitesService.getDefaultSite();
            if (defaultSite) {
                if (defSite == null) {
                    sitesService.setDefaultSite(site);
                } else if (!defSite.getSiteKey().equals(site.getSiteKey())) {
                    sitesService.setDefaultSite(site);
                }
            } else {
                if (defSite != null && defSite.getSiteKey().equals(site.getSiteKey())) {
                    sitesService.setDefaultSite(null);
                }
            }
            session.save();
            // redirect...
            return ActionResult.OK_JSON;
        } catch (JahiaException ex) {
            logger.warn("Error while processing site edition", ex);
            result.put("warn", getMessage(renderContext.getUILocale(),"label.error.processingRequestError"));
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
        }
    }


}
