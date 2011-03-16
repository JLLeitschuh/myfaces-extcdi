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
package org.apache.myfaces.extensions.cdi.test.owb;

import org.apache.myfaces.extensions.cdi.core.api.projectstage.ProjectStage;
import org.apache.myfaces.extensions.cdi.core.impl.projectstage.ProjectStageProducer;
import org.apache.myfaces.extensions.cdi.test.spi.CdiTestContainer;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * @author Gerhard Petracek
 */
public abstract class AbstractOpenWebBeans10TestContainer implements CdiTestContainer
{
    protected org.apache.webbeans.cditest.CdiTestContainer testContainer;

    public void initEnvironment()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    public void startContainer()
    {
        try
        {
            this.testContainer.bootContainer();
        }
        catch (Exception e)
        {
            //TODO
            throw new RuntimeException(e);
        }
    }

    public void stopContainer()
    {
        try
        {
            this.testContainer.shutdownContainer();
        }
        catch (Exception e)
        {
            //TODO
            throw new RuntimeException(e);
        }
    }

    public void startContexts()
    {
        try
        {
            this.testContainer.startContexts();
        }
        catch (Exception e)
        {
            //TODO
            throw new RuntimeException(e);
        }
    }

    public void stopContexts()
    {
        try
        {
            this.testContainer.stopContexts();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public BeanManager getBeanManager()
    {
        this.testContainer.getBeanManager();
        throw new IllegalStateException("not implemented");
    }

    public <T> T injectFields(T instance)
    {
        BeanManager beanManager = getBeanManager();
        CreationalContext creationalContext = beanManager.createCreationalContext(null);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(getClass());
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(instance, creationalContext);
        return instance;
    }
}