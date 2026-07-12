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

import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RolesHandler} principal-scope validation.
 */
public class RolesHandlerTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private JahiaUserManagerService userManagerService;
    @Mock
    private JahiaGroupManagerService groupManagerService;
    @Mock
    private JCRUserNode siteUser;
    @Mock
    private JCRUserNode rootUser;
    @Mock
    private JCRGroupNode group;

    private RolesHandler handler;

    @Before
    public void setUp() {
        handler = new RolesHandler();
        handler.userManagerService = userManagerService;
        handler.groupManagerService = groupManagerService;
    }

    @Test
    public void getSiteKeyExtractsSiteFromNodePath() {
        handler.setNodePath("/sites/mysite/home/page");
        assertEquals("mysite", handler.getSiteKey());
        handler.setNodePath("/sites/mysite");
        assertEquals("mysite", handler.getSiteKey());
    }

    @Test
    public void getSiteKeyIsNullForServerContext() {
        handler.setNodePath("/");
        assertNull(handler.getSiteKey());
        handler.setNodePath("/modules/foo");
        assertNull(handler.getSiteKey());
    }

    @Test
    public void acceptsUserResolvableWithinSiteScope() {
        when(userManagerService.lookupUser("alice", "mysite")).thenReturn(siteUser);
        when(siteUser.isRoot()).thenReturn(false);
        assertTrue(handler.isPrincipalInScope("u:alice", "mysite"));
    }

    @Test
    public void rejectsUserNotResolvableWithinSiteScope() {
        // a user local to another site does not resolve from this site (global-first lookup returns null)
        when(userManagerService.lookupUser("xbob", "mysite")).thenReturn(null);
        assertFalse(handler.isPrincipalInScope("u:xbob", "mysite"));
    }

    @Test
    public void rejectsRootSuperUser() {
        when(userManagerService.lookupUser("root", "mysite")).thenReturn(rootUser);
        when(rootUser.isRoot()).thenReturn(true);
        assertFalse(handler.isPrincipalInScope("u:root", "mysite"));
    }

    @Test
    public void acceptsGlobalGroupViaFallback() {
        when(groupManagerService.lookupGroup("mysite", "users")).thenReturn(null);
        when(groupManagerService.lookupGroup(null, "users")).thenReturn(group);
        assertTrue(handler.isPrincipalInScope("g:users", "mysite"));
    }

    @Test
    public void rejectsGroupNotResolvable() {
        when(groupManagerService.lookupGroup("mysite", "ghost")).thenReturn(null);
        when(groupManagerService.lookupGroup(null, "ghost")).thenReturn(null);
        assertFalse(handler.isPrincipalInScope("g:ghost", "mysite"));
    }

    @Test
    public void serverContextAcceptsGlobalUser() {
        when(userManagerService.lookupUser("alice")).thenReturn(siteUser);
        when(siteUser.isRoot()).thenReturn(false);
        assertTrue(handler.isPrincipalInScope("u:alice", null));
    }

    @Test
    public void serverContextRejectsSiteLocalUser() {
        // a site-local user is not found by the global-only lookup used at server/system level
        when(userManagerService.lookupUser("bob")).thenReturn(null);
        assertFalse(handler.isPrincipalInScope("u:bob", null));
    }

    @Test
    public void serverContextAcceptsGlobalGroup() {
        when(groupManagerService.lookupGroup(null, "administrators")).thenReturn(group);
        assertTrue(handler.isPrincipalInScope("g:administrators", null));
    }

    @Test
    public void serverContextRejectsSiteLocalGroup() {
        when(groupManagerService.lookupGroup(null, "site-editors")).thenReturn(null);
        assertFalse(handler.isPrincipalInScope("g:site-editors", null));
    }

    @Test
    public void rejectsMalformedPrincipal() {
        assertFalse(handler.isPrincipalInScope(null, "mysite"));
        assertFalse(handler.isPrincipalInScope("x", "mysite"));
        assertFalse(handler.isPrincipalInScope("z:foo", "mysite"));
    }
}
