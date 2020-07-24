/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.defaultmodule.actions;

import org.jahia.bin.Action;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.jcr.AccessDeniedException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Consumer;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Testing {@link ChainAction}
 *
 * @author cmoitrier
 */
@SuppressWarnings({"java:S112", "java:S1192"})
public final class ChainActionTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock JahiaTemplateManagerService templateService;
    @Mock HttpServletRequest httpRequest;
    @Mock RenderContext renderContext;
    @Mock Resource resource;
    @Mock JCRSessionWrapper jcrSessionWrapper;
    @Mock URLResolver urlResolver;

    @Before
    public void setup() {
        when(jcrSessionWrapper.isSystem()).thenReturn(false);
    }

    @Test
    public void testAllActionsExecuted() throws Exception {
        Action action1 = mockAction("action1");
        Action action2 = mockAction("action2");
        registerActions(action1, action2);

        ChainAction chainAction = new ChainAction();
        chainAction.setTemplateService(templateService);

        Map<String, List<String>> parameters = mapOf(ChainAction.CHAIN_OF_ACTION, Arrays.asList("action1,action2"));
        chainAction.doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);

        verify(action1).doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);
        verify(action2).doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);
    }

    @Test
    public void testProtectedActionIsNotExecutedWhenUnauthenticated() throws Exception {
        when(renderContext.isLoggedIn()).thenReturn(false);

        Action action1 = mockAction("action1");
        Action action2 = mockAction("action2", a -> when(a.isRequireAuthenticatedUser()).thenReturn(true));
        registerActions(action1, action2);

        ChainAction chainAction = new ChainAction();
        chainAction.setTemplateService(templateService);

        Map<String, List<String>> parameters = mapOf(ChainAction.CHAIN_OF_ACTION, Arrays.asList("action1,action2"));
        Exception e = assertThrows(AccessDeniedException.class, () ->
                chainAction.doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver)
        );
        assertEquals("Action 'action2' requires an authenticated user", e.getMessage());

        verify(action1).doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);
        verify(action2, never()).doExecute(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testProtectedActionIsExecutedWhenAuthenticated() throws Exception {
        when(renderContext.isLoggedIn()).thenReturn(true);

        Action action1 = mockAction("action1");
        Action action2 = mockAction("action2", a -> when(a.isRequireAuthenticatedUser()).thenReturn(true));
        registerActions(action1, action2);

        ChainAction chainAction = new ChainAction();
        chainAction.setTemplateService(templateService);

        Map<String, List<String>> parameters = mapOf(ChainAction.CHAIN_OF_ACTION, Arrays.asList("action1,action2"));
        chainAction.doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);

        verify(action1).doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);
        verify(action2).doExecute(httpRequest, renderContext, resource, jcrSessionWrapper, parameters, urlResolver);
    }

    private Action mockAction(String name) {
        Action action = mock(Action.class);
        when(action.getName()).thenReturn(name);
        when(action.getRequiredMethods()).thenReturn(null);
        return action;
    }

    private Action mockAction(String name, Consumer<Action> consumer) {
        Action action = mockAction(name);
        consumer.accept(action);
        return action;
    }

    private void registerActions(Action ...actions) {
        Map<String, Action> map = Arrays.stream(actions).collect(toMap(Action::getName, identity()));
        when(templateService.getActions()).thenReturn(map);
    }

    private static <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

}
