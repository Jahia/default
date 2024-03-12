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

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.*;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Action handler that is capable of executing a chain of specified action.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 11 mars 2010
 */
public class ChainAction extends Action implements InitializingBean {

    private JahiaTemplateManagerService templateService;
    private DefaultPostAction defaultPostAction;
    private URLResolverFactory urlResolverFactory;

    public static final String ACTION_NAME = "chain";
    public static final String CHAIN_OF_ACTION = "chainOfAction";

    public ChainAction() {
        setName(ACTION_NAME);
    }

    public void setTemplateService(JahiaTemplateManagerService templateService) {
        this.templateService = templateService;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void setDefaultPostAction(DefaultPostAction defaultPostAction) {
        this.defaultPostAction = defaultPostAction;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
                                  Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver)
            throws Exception {
        List<String> chainOfactions = parameters.get(CHAIN_OF_ACTION);
        if (chainOfactions != null && StringUtils.isNotEmpty(chainOfactions.get(0))) {
            String[] actions = chainOfactions.get(0).split(",");
            Map<String, Action> actionsMap = templateService.getActions();
            ActionResult result = null;
            String path = null;
            for (String actionToDo : actions) {
                if (DefaultPostAction.ACTION_NAME.equals(actionToDo)) {
                    checkRequirements(defaultPostAction, renderContext, urlResolver, session);
                    String s = urlResolver.getUrlPathInfo().replace(".chain.do", "/*");
                    URLResolver resolver = urlResolverFactory.createURLResolver(s,req.getServerName(), req);
                    resolver.setSiteKey(urlResolver.getSiteKey());
                    result = defaultPostAction.doExecute(req, renderContext, resource, session, parameters, resolver);
                } else {
                    Action action = actionsMap.get(actionToDo);
                    checkRequirements(action, renderContext, urlResolver, session);
                    result = action.doExecute(req, renderContext, resource, session, parameters, urlResolver);
                    if (actionToDo.equals("redirect") && result != null && result.getUrl() != null) {
                    	path = result.getUrl();
                    }
                }
            }
            if (path != null) {
            	result.setUrl(path);
            }
            return result;
        }
        return ActionResult.BAD_REQUEST;
    }

    private void checkRequirements(Action action, RenderContext renderContext, URLResolver urlResolver,
            JCRSessionWrapper session) throws RepositoryException {
        // check that the action is not executed as SystemAction
        if (!session.isSystem()) {
            // not a system action -> check all the requirements
            Render.checkActionRequirements(action, renderContext, urlResolver);
        }
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        Render.getReservedParameters().add(CHAIN_OF_ACTION);
    }
}
