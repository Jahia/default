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
package org.jahia.modules.defaultmodule.actions.admin;

import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Delete a site.
 */
public class AdminDeleteSiteAction extends AdminSiteAction {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, JahiaSite site, JCRSessionWrapper session, Map<String, List<String>> parameters) throws Exception {
        // now let's check if this site is the default site, in which case
        // we need to change the default site to another one.
        JahiaSite defSite = sitesService.getDefaultSite();

        // first let's build a list of the all the sites except the
        // current one.
        List<JCRSiteNode> otherSites = new ArrayList<JCRSiteNode>();
        for (JCRSiteNode curSite: sitesService.getSitesNodeList()) {
            if (!curSite.getSiteKey().equals(site.getSiteKey())) {
                otherSites.add(curSite);
            }
        }
        if (defSite == null) {
            // no default site, let's assign once that isn't the current
            // one being deleted.
            if (otherSites.size() > 0) {
                sitesService.setDefaultSite(sitesService.getSite(otherSites.get(0).getName()));
            }
        } else if (defSite.getSiteKey().equals(site.getSiteKey())) {
            // the default site IS the site being deleted, let's set
            // another site as a default site.
            if (otherSites.size() > 0) {
                sitesService.setDefaultSite(sitesService.getSite(otherSites.get(0).getName()));
            } else {
                sitesService.setDefaultSite(null);
            }
        }

        // switch staging and versioning to false.
        sitesService.updateSystemSitePermissions(site);

        //remove site definition
        sitesService.removeSite(site);

        return ActionResult.OK_JSON;
    }


}
