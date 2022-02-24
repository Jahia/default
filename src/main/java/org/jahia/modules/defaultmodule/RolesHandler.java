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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.SearchCriteria;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.util.*;

public class RolesHandler implements Serializable {
    private static final long serialVersionUID = 2485636561921483297L;


    private static final Logger logger = LoggerFactory.getLogger(RolesHandler.class);


    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    @Autowired
    private transient JCRPublicationService publicationService;

    private String workspace;

    private Locale locale;

    private Locale fallbackLocale;

    private String roleGroup;

    private String searchType = "users";

    private String role;

    private String nodePath;

    private List<String> roles;

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public Map<JCRNodeWrapper, List<JCRNodeWrapper>> getRoles() throws Exception {
        Map<String, JCRNodeWrapper> rolesFromName = new HashMap<String, JCRNodeWrapper>();
        Map<JCRNodeWrapper, List<JCRNodeWrapper>> result = new TreeMap<JCRNodeWrapper, List<JCRNodeWrapper>>(new Comparator<JCRNodeWrapper>() {
            @Override
            public int compare(JCRNodeWrapper jcrNodeWrapper, JCRNodeWrapper jcrNodeWrapper2) {
                return jcrNodeWrapper.getDisplayableName().compareTo(jcrNodeWrapper2.getDisplayableName());
            }
        });
        final JCRSessionWrapper defaultSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, locale, fallbackLocale);
        QueryManager qm = defaultSession.getWorkspace().getQueryManager();
        if (role != null) {
            Query q = qm.createQuery("select * from [jnt:role] where localname()='" + JCRContentUtils.sqlEncode(role) + "'", Query.JCR_SQL2);
            getRoles(q, rolesFromName, result);
        } else if (roles == null) {
            Query q = qm.createQuery("select * from [jnt:role] where [j:roleGroup]='" + JCRContentUtils.sqlEncode(roleGroup) + "'", Query.JCR_SQL2);
            getRoles(q, rolesFromName, result);
        } else {
            for (String r : roles) {
                Query q = qm.createQuery("select * from [jnt:role] where localname()='" + JCRContentUtils.sqlEncode(r) + "'", Query.JCR_SQL2);
                getRoles(q, rolesFromName, result);
            }
        }

        final JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);
        JCRNodeWrapper node = s.getNode(nodePath);
        Map<String, List<String[]>> acl = node.getAclEntries();

        String siteKey = nodePath.startsWith("/sites/") ? StringUtils.substringBefore(StringUtils.substringAfter(nodePath,"/sites/"),"/") : null;

        for (Map.Entry<String, List<String[]>> entry : acl.entrySet()) {
            JCRNodeWrapper p = null;
            if (entry.getKey().startsWith("u:")) {
                p = userManagerService.lookupUser(entry.getKey().substring(2),siteKey);
            } else if (entry.getKey().startsWith("g:")) {
                if (siteKey != null) {
                    p = groupManagerService.lookupGroup(siteKey, entry.getKey().substring(2));
                }
                if (p == null) {
                    p = groupManagerService.lookupGroup(null, entry.getKey().substring(2));
                }
            }
            if (p != null) {
                final List<String[]> value = entry.getValue();
                Collections.reverse(value);
                for (String[] strings : value) {
                    String role = strings[2];

                    if (strings[1].equals("GRANT") && rolesFromName.containsKey(role) && !result.get(rolesFromName.get(role)).contains(p)) {
                        result.get(rolesFromName.get(role)).add(p);
                    } else if (strings[1].equals("DENY") && rolesFromName.containsKey(role)) {
                        result.get(rolesFromName.get(role)).remove(p);
                    }
                }
            }
        }

        return result;
    }

    private void getRoles(Query q, Map<String, JCRNodeWrapper> rolesFromName, Map<JCRNodeWrapper, List<JCRNodeWrapper>> m) throws RepositoryException {
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            m.put(next, new ArrayList<JCRNodeWrapper>());
            rolesFromName.put(next.getName(), next);
        }
    }

    public void setContext(JCRNodeWrapper node, RenderContext context) throws RepositoryException {
        if (node.hasProperty("roles")) {
            roles = new ArrayList<String>();
            for (Value value : node.getProperty("roles").getValues()) {
                roles.add(value.getString());
            }
        } else {
            roles = null;
        }
        if (node.hasProperty("roleGroup")) {
            roleGroup = node.getProperty("roleGroup").getString();
        }
        if (node.hasProperty("contextNodePath")) {
            nodePath = node.getProperty("contextNodePath").getString();
        } else {
            nodePath = context.getMainResource().getNode().getPath();
        }
        workspace = node.getSession().getWorkspace().getName();
        locale = node.getSession().getLocale();
        fallbackLocale = node.getSession().getFallbackLocale();
    }

    public List<JCRNodeWrapper> getRoleMembers() throws Exception {
        Map<JCRNodeWrapper, List<JCRNodeWrapper>> r = getRoles();
        return r.size() > 0 ? r.entrySet().iterator().next().getValue() : new ArrayList<JCRNodeWrapper>();
    }

    public void grantRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);
        for (String principal : principals) {
            session.getNode(nodePath).grantRoles(principal, Collections.singleton(role));
        }
        session.save();
        // Publish the node acls
        if (Constants.EDIT_WORKSPACE.equals(workspace) && session.getNode(nodePath).hasNode("j:acl")) {
            publicationService.publishByMainId(session.getNode(nodePath).getNode("j:acl").getIdentifier());
        }
    }

    public void revokeRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);

        Map<String, String> roles = new HashMap<String, String>();
        for (String principal : principals) {
            List<String[]> entries = session.getNode(nodePath).getAclEntries().get(principal);
            for (String[] strings : entries) {
                if (!role.equals(strings[2])) {
                    roles.put(strings[2], strings[1]);
                } else if (!strings[0].equals(nodePath)) {
                    roles.put(strings[2], "DENY");
                }
            }
            session.getNode(nodePath).revokeRolesForPrincipal(principal);
            session.getNode(nodePath).changeRoles(principal, roles);
        }

        session.save();
        // Publish the node acls
        if (Constants.EDIT_WORKSPACE.equals(workspace) && session.getNode(nodePath).hasNode("j:acl")) {
            publicationService.publishByMainId(session.getNode(nodePath).getNode("j:acl").getIdentifier());
        }
    }

    /**
     * Returns an empty (newly initialized) search criteria bean.
     *
     * @return an empty (newly initialized) search criteria bean
     */
    public SearchCriteria initCriteria(RequestContext ctx) {
        return new SearchCriteria(null);
    }


    public Map<String, JCRStoreProvider> getProviders() {
        Map<String, JCRStoreProvider> providers = new LinkedHashMap<String, JCRStoreProvider>();

        return providers;
    }

    /**
     * Performs the group search with the specified search criteria and returns the list of matching groups.
     *
     * @param searchCriteria current search criteria
     * @return the list of groups, matching the specified search criteria
     */
    public Set<JCRNodeWrapper> searchNewMembers(SearchCriteria searchCriteria) throws RepositoryException {
        long timer = System.currentTimeMillis();

        Set<JCRNodeWrapper> searchResult;
        if (searchType.equals("users")) {
            String siteKey = null;
            if (nodePath.startsWith("/sites/")) {
                siteKey = StringUtils.substringAfter(nodePath, "/sites/");
            }
            searchResult = new HashSet<JCRNodeWrapper>(PrincipalViewHelper.getSearchResult(searchCriteria.getSearchIn(),
                    siteKey, searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                    searchCriteria.getProviders()));
        } else {
            String siteKey = null;
            if (nodePath.startsWith("/sites/")) {
                siteKey = StringUtils.substringAfter(nodePath, "/sites/");
            }
            searchResult = new HashSet<JCRNodeWrapper>(PrincipalViewHelper.getGroupSearchResult(searchCriteria.getSearchIn(), siteKey,
                    searchCriteria.getSearchString(), searchCriteria.getProperties(),
                    searchCriteria.getStoredOn(), searchCriteria.getProviders()));
        }

        logger.info("Found {} groups in {} ms", searchResult.size(), System.currentTimeMillis() - timer);
        return searchResult;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchType() {
        return searchType;
    }

    /**
     * Decodes the provided parameter values.
     *
     * @param params
     *            the values to be decoded
     * @return the array of decoded values
     */
    public String[] urlDecode(String[] params) {
        if (params == null || params.length == 0) {
            return params;
        }

        String[] decoded = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            decoded[i] = WebUtils.urlDecode(params[i]);
        }

        return decoded;
    }
}
