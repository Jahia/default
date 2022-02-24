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
package org.jahia.modules.osgi;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class JahiaModuleConfigRegistry implements JahiaModuleAware {

    private JahiaTemplatesPackage module;
    private ManagedService managedService;
    private ManagedServiceFactory managedServiceFactory;
    private ServiceRegistration serviceRegistration;

    public void start() {
        Dictionary props = new Hashtable();
        if (managedService != null) {
            props.put(Constants.SERVICE_PID, managedService.getClass().getName());
            serviceRegistration = module.getBundle().getBundleContext().registerService(ManagedService.class.getName(), managedService, props);
        } else if (managedServiceFactory != null) {
            props.put(Constants.SERVICE_PID, managedServiceFactory.getName());
            serviceRegistration = module.getBundle().getBundleContext().registerService(ManagedServiceFactory.class.getName(), managedServiceFactory, props);
        }
    }

    public void stop() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }

    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }

    public void setManagedService(ManagedService managedService) {
        this.managedService = managedService;
    }

    public void setManagedServiceFactory(ManagedServiceFactory managedServiceFactory) {
        this.managedServiceFactory = managedServiceFactory;
    }

}
