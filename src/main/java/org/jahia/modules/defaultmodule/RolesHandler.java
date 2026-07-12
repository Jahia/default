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
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSitesService;
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

    // internal JCR path prefix (not a network URI); the "/" is a JCR path delimiter
    @SuppressWarnings("java:S1075")
    private static final String SITES_PATH_PREFIX = JahiaSitesService.SITES_JCR_PATH + "/";

    private static final String ACL_NODE_NAME = "j:acl";


    @Autowired
    transient JahiaUserManagerService userManagerService;

    @Autowired
    transient JahiaGroupManagerService groupManagerService;

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

        String siteKey = getSiteKey();

        for (Map.Entry<String, List<String[]>> entry : acl.entrySet()) {
            JCRNodeWrapper p = resolvePrincipal(entry.getKey(), siteKey);
            if (p != null) {
                applyRoleMembership(entry.getValue(), p, rolesFromName, result);
            }
        }

        return result;
    }

    /** Resolves an ACL principal key ({@code "u:name"} / {@code "g:name"}) to its user/group node, or {@code null}. */
    private JCRNodeWrapper resolvePrincipal(String aclKey, String siteKey) {
        if (aclKey.startsWith("u:")) {
            return userManagerService.lookupUser(aclKey.substring(2), siteKey);
        }
        if (aclKey.startsWith("g:")) {
            JCRNodeWrapper group = siteKey != null ? groupManagerService.lookupGroup(siteKey, aclKey.substring(2)) : null;
            return group != null ? group : groupManagerService.lookupGroup(null, aclKey.substring(2));
        }
        return null;
    }

    /** Folds a principal's ACL entries (GRANT adds, DENY removes) into the role→members {@code result} map. */
    private void applyRoleMembership(List<String[]> aces, JCRNodeWrapper principal, Map<String, JCRNodeWrapper> rolesFromName,
            Map<JCRNodeWrapper, List<JCRNodeWrapper>> result) {
        Collections.reverse(aces);
        for (String[] ace : aces) {
            JCRNodeWrapper roleNode = rolesFromName.get(ace[2]);
            if (roleNode == null) {
                continue;
            }
            if ("GRANT".equals(ace[1]) && !result.get(roleNode).contains(principal)) {
                result.get(roleNode).add(principal);
            } else if ("DENY".equals(ace[1])) {
                result.get(roleNode).remove(principal);
            }
        }
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
        final String siteKey = getSiteKey();
        for (String principal : principals) {
            if (!isPrincipalInScope(principal, siteKey)) {
                // only grant to a principal valid in this context: within the administered site's scope,
                // or — at server/system level (no site) — a server-global principal; ignore anything else
                logger.warn("Ignoring role grant of '{}' on '{}' to out-of-scope principal '{}'", role, nodePath, principal);
                continue;
            }
            session.getNode(nodePath).grantRoles(principal, Collections.singleton(role));
        }
        session.save();
        // Publish the node acls
        if (Constants.EDIT_WORKSPACE.equals(workspace) && session.getNode(nodePath).hasNode(ACL_NODE_NAME)) {
            publicationService.publishByMainId(session.getNode(nodePath).getNode(ACL_NODE_NAME).getIdentifier());
        }
    }

    public void revokeRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);
        final String siteKey = getSiteKey();

        for (String principal : principals) {
            revokeRoleForPrincipal(session, principal, siteKey);
        }

        session.save();
        // Publish the node acls
        if (Constants.EDIT_WORKSPACE.equals(workspace) && session.getNode(nodePath).hasNode(ACL_NODE_NAME)) {
            publicationService.publishByMainId(session.getNode(nodePath).getNode(ACL_NODE_NAME).getIdentifier());
        }
    }

    private void revokeRoleForPrincipal(JCRSessionWrapper session, String principal, String siteKey) throws RepositoryException {
        if (!isPrincipalInScope(principal, siteKey)) {
            // mirror grantRole: never act on a principal outside this context's scope. This is also what the
            // UI enforces implicitly — out-of-scope principals are not resolvable in this context, so they
            // are never listed and never actionable (grant or revoke).
            logger.warn("Ignoring role revoke of '{}' on '{}' for out-of-scope principal '{}'", role, nodePath, principal);
            return;
        }
        List<String[]> entries = session.getNode(nodePath).getAclEntries().get(principal);
        if (entries == null) {
            // only an existing ACL entry can be revoked; ignore principals not present on the node
            // (also avoids a NullPointerException on an arbitrary/unknown principal)
            logger.warn("Ignoring role revoke of '{}' on '{}' for principal '{}': no matching ACL entry", role, nodePath, principal);
            return;
        }
        Map<String, String> roleChanges = new HashMap<>();
        for (String[] strings : entries) {
            if (!role.equals(strings[2])) {
                roleChanges.put(strings[2], strings[1]);
            } else if (!strings[0].equals(nodePath)) {
                roleChanges.put(strings[2], "DENY");
            }
        }
        session.getNode(nodePath).revokeRolesForPrincipal(principal);
        session.getNode(nodePath).changeRoles(principal, roleChanges);
    }

    /**
     * @return the site key when {@link #nodePath} is under {@code /sites/<key>}, otherwise {@code null}
     *         (a server-level context, where no per-site principal restriction applies).
     */
    String getSiteKey() {
        if (nodePath == null || !nodePath.startsWith(SITES_PATH_PREFIX)) {
            return null;
        }
        String key = StringUtils.substringBefore(StringUtils.substringAfter(nodePath, SITES_PATH_PREFIX), "/");
        return StringUtils.isEmpty(key) ? null : key;
    }

    /**
     * Tells whether a principal ({@code "u:name"} / {@code "g:name"}) may be granted a role in the current
     * context.
     * <ul>
     *   <li>When administering a <b>site</b> ({@code siteKey != null}): the principal must resolve within
     *       that site's scope — its own users/groups or a server-global principal (global-first, then the
     *       site). A principal local to a <em>different</em> site does not resolve and is rejected.</li>
     *   <li>At <b>server/system</b> level ({@code siteKey == null}): only a server-global principal is
     *       accepted; any site-scoped principal is rejected.</li>
     * </ul>
     * The system super-user is always refused. Resolution goes through the cached, negative-cached
     * {@code lookupUser}/{@code lookupGroup} path already used to display role members, so a global-only
     * check costs a single cached lookup (no site tree walk). Global lookups use {@code lookupUser(name)}
     * / {@code lookupGroup(null, name)}, which query only the global {@code /users} and {@code /groups}.
     */
    boolean isPrincipalInScope(String principal, String siteKey) {
        if (principal == null || principal.length() < 3) {
            return false;
        }
        String name = principal.substring(2);
        if (principal.startsWith("u:")) {
            JCRUserNode user = siteKey != null
                    ? userManagerService.lookupUser(name, siteKey)  // this site or global (global-first)
                    : userManagerService.lookupUser(name);          // global only
            return user != null && !user.isRoot();
        }
        if (principal.startsWith("g:")) {
            JCRGroupNode group = siteKey != null ? groupManagerService.lookupGroup(siteKey, name) : null;
            if (group == null) {
                group = groupManagerService.lookupGroup(null, name); // a server-global group is valid in both contexts
            }
            return group != null;
        }
        return false;
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
            if (nodePath.startsWith(SITES_PATH_PREFIX)) {
                siteKey = StringUtils.substringAfter(nodePath, SITES_PATH_PREFIX);
            }
            searchResult = new HashSet<JCRNodeWrapper>(PrincipalViewHelper.getSearchResult(searchCriteria.getSearchIn(),
                    siteKey, searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                    searchCriteria.getProviders()));
        } else {
            String siteKey = null;
            if (nodePath.startsWith(SITES_PATH_PREFIX)) {
                siteKey = StringUtils.substringAfter(nodePath, SITES_PATH_PREFIX);
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
