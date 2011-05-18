/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.extensions.cdi.jsf.alternative;

import org.apache.myfaces.extensions.cdi.core.api.startup.event.StartupEvent;
import org.apache.myfaces.extensions.cdi.core.api.util.ClassUtils;
import org.apache.myfaces.extensions.cdi.core.impl.AbstractStartupObserver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

/**
 * @author Gerhard Petracek
 */
@ApplicationScoped
public class AlternativeJsfImplModuleConfigStartupObserver extends AbstractStartupObserver
{
    protected void logCoreConfiguration(@Observes StartupEvent startupEvent)
    {
        String moduleVersion = detectModuleVersion();

        StringBuilder info = new StringBuilder("[Started] MyFaces CODI (Extensions CDI) Alternative JSF-Impl Module");
        info.append(moduleVersion);
        info.append(separator);

        this.logger.info(info.toString());
    }

    protected String detectModuleVersion()
    {
        String version = ClassUtils.getJarVersion(AlternativeJsfImplModuleConfigStartupObserver.class);

        if(version != null && !version.startsWith("null"))
        {
            return " v" + version;
        }
        return "";
    }
}