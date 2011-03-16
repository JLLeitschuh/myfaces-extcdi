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
package org.apache.myfaces.extensions.cdi.jsf.impl.config.view;

import org.apache.myfaces.extensions.cdi.core.api.config.view.DefaultErrorView;
import org.apache.myfaces.extensions.cdi.core.api.config.view.ViewConfig;
import org.apache.myfaces.extensions.cdi.core.api.provider.BeanManagerProvider;
import org.apache.myfaces.extensions.cdi.core.api.security.AccessDecisionVoter;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.Page;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PageBean;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PageBeanDescriptor;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.ViewConfigDescriptor;
import org.apache.myfaces.extensions.cdi.jsf.impl.config.view.spi.EditableViewConfigDescriptor;
import org.apache.myfaces.extensions.cdi.jsf.impl.config.view.spi.LifecycleAwarePageBeanDescriptor;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.ObjectInputStream;
import java.io.IOException;

import static org.apache.myfaces.extensions.cdi.core.impl.util.CodiUtils.getContextualReferenceByName;

/**
 * @author Gerhard Petracek
 */
public class DefaultViewConfigDescriptor implements EditableViewConfigDescriptor
{
    private final String viewId;

    private final Class<? extends ViewConfig> viewDefinitionClass;

    private final Page.NavigationMode navigationMode;

    private List<PageBeanDescriptor> pageBeanDescriptors;

    //security
    private final List<Class<? extends AccessDecisionVoter>> accessDecisionVoters;
    private final Class<? extends ViewConfig> customErrorView;

    private Page.ViewParameterMode viewParameterMode;
    //meta-data
    private List<Annotation> metaDataList;

    private boolean simpleEntryMode = false;

    private transient BeanManager beanManager;

    public DefaultViewConfigDescriptor(String viewId,
                                       Class<? extends ViewConfig> viewDefinitionClass,
                                       Page.NavigationMode navigationMode,
                                       Page.ViewParameterMode viewParameterMode,
                                       List<Class<? extends AccessDecisionVoter>> accessDecisionVoters,
                                       Class<? extends ViewConfig> errorView,
                                       List<Annotation> metaDataList)
    {
        this.viewId = viewId;
        this.viewDefinitionClass = viewDefinitionClass;
        this.navigationMode = navigationMode;
        this.viewParameterMode = viewParameterMode;

        this.metaDataList = metaDataList;

        pageBeanDescriptors = Collections.unmodifiableList(findPageBeanDefinitions(viewDefinitionClass));
        //TODO validate view-id

        //noinspection unchecked
        this.accessDecisionVoters = accessDecisionVoters;

        if(errorView != null)
        {
            this.customErrorView = errorView;
        }
        else
        {
            this.customErrorView = DefaultErrorView.class;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getViewId()
    {
        return viewId;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends ViewConfig> getViewConfig()
    {
        return viewDefinitionClass;
    }

    /**
     * {@inheritDoc}
     */
    public Page.NavigationMode getNavigationMode()
    {
        return navigationMode;
    }

    /**
     * {@inheritDoc}
     */
    public Page.ViewParameterMode getViewParameterMode()
    {
        return viewParameterMode;
    }

    /**
     * {@inheritDoc}
     */
    public List<PageBeanDescriptor> getPageBeanConfigs()
    {
        return pageBeanDescriptors;
    }

    /**
     * {@inheritDoc}
     */
    public void invokeInitViewMethods()
    {
        for(PageBeanDescriptor beanEntry : getPageBeanConfigs())
        {
            if(beanEntry instanceof LifecycleAwarePageBeanDescriptor)
            {
                processCallbacks(beanEntry, ((LifecycleAwarePageBeanDescriptor)beanEntry).getInitViewMethods());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invokePrePageActionMethods()
    {
        for(PageBeanDescriptor beanEntry : getPageBeanConfigs())
        {
            if(beanEntry instanceof LifecycleAwarePageBeanDescriptor)
            {
                processCallbacks(beanEntry, ((LifecycleAwarePageBeanDescriptor)beanEntry).getPrePageActionMethods());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invokePreRenderViewMethods()
    {
        for(PageBeanDescriptor beanEntry : getPageBeanConfigs())
        {
            if(beanEntry instanceof LifecycleAwarePageBeanDescriptor)
            {
                processCallbacks(beanEntry, ((LifecycleAwarePageBeanDescriptor)beanEntry).getPreRenderViewMethods());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invokePostRenderViewMethods()
    {
        for(PageBeanDescriptor beanEntry : getPageBeanConfigs())
        {
            if(beanEntry instanceof LifecycleAwarePageBeanDescriptor)
            {
                processCallbacks(beanEntry, ((LifecycleAwarePageBeanDescriptor)beanEntry).getPostRenderViewMethods());
            }
        }
    }

    private void processCallbacks(PageBeanDescriptor beanEntry, List<Method> methodList)
    {
        Object bean;
        if (!methodList.isEmpty())
        {
            //TODO provide a detailed error message in case of a missing bean
            bean = getContextualReferenceByName(getBeanManager(), beanEntry.getBeanName(), Object.class);

            if (bean == null)
            {
                return;
            }

            for (Method callbackMethod : methodList)
            {
                invokeMethod(bean, callbackMethod);
            }
        }
    }

    private void invokeMethod(Object bean, Method preProcessMethod)
    {
        try
        {
            preProcessMethod.setAccessible(true);
            preProcessMethod.invoke(bean);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Class<? extends AccessDecisionVoter>> getAccessDecisionVoters()
    {
        return Collections.unmodifiableList(this.accessDecisionVoters);
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends ViewConfig> getErrorView()
    {
        return customErrorView;
    }

    /**
     * {@inheritDoc}
     */
    public List<Annotation> getMetaData()
    {
        return metaDataList;
    }

    /**
     * {@inheritDoc}
     */
    public void addPageBean(Class pageBeanClass)
    {
        List<PageBeanDescriptor> newList = new ArrayList<PageBeanDescriptor>(this.pageBeanDescriptors);

        PageBeanDescriptor newEntry = new DefaultPageBeanDescriptor(getBeanName(pageBeanClass) , pageBeanClass);

        newList.add(newEntry);

        this.pageBeanDescriptors = Collections.unmodifiableList(newList);
    }

    void activateSimpleEntryMode()
    {
        this.simpleEntryMode = true;
    }

    boolean isSimpleEntryMode()
    {
        return simpleEntryMode;
    }

    private List<PageBeanDescriptor> findPageBeanDefinitions(Class<? extends ViewConfig> viewDefinitionClass)
    {
        if(!viewDefinitionClass.isAnnotationPresent(PageBean.class) &&
                !viewDefinitionClass.isAnnotationPresent(PageBean.List.class))
        {
            return Collections.emptyList();
        }

        List<PageBeanDescriptor> result = new ArrayList<PageBeanDescriptor>();

        if(viewDefinitionClass.isAnnotationPresent(PageBean.class))
        {
            result.add(extractBeanEntry(viewDefinitionClass.getAnnotation(PageBean.class)));
        }

        if(viewDefinitionClass.isAnnotationPresent(PageBean.List.class))
        {
            result.addAll(extractBeanEntries(viewDefinitionClass.getAnnotation(PageBean.List.class)));
        }

        return result;
    }

    private List<PageBeanDescriptor> extractBeanEntries(PageBean.List pageBeanList)
    {
        List<PageBeanDescriptor> result = new ArrayList<PageBeanDescriptor>();
        for(PageBean pageBean : pageBeanList.value())
        {
            result.add(extractBeanEntry(pageBean));
        }
        return result;
    }

    private PageBeanDescriptor extractBeanEntry(PageBean pageBean)
    {
        if(!"".equals(pageBean.name()))
        {
            return new DefaultPageBeanDescriptor(pageBean.name(), pageBean.value());
        }

        Class<?> pageBeanClass = pageBean.value();
        String pageBeanName = null;

        //TODO allow indirect usage of @Named
        pageBeanName = getBeanName(pageBeanClass);

        return new DefaultPageBeanDescriptor(pageBeanName, pageBeanClass);
    }

    private String getBeanName(Class<?> pageBeanClass)
    {
        if(pageBeanClass.isAnnotationPresent(Named.class))
        {
            String beanName = pageBeanClass.getAnnotation(Named.class).value();

            if(!"".equals(beanName))
            {
                return beanName;
            }
        }

        return Introspector.decapitalize(pageBeanClass.getSimpleName());
    }

    private BeanManager getBeanManager()
    {
        if(this.beanManager == null)
        {
            this.beanManager = BeanManagerProvider.getInstance().getBeanManager();
        }

        return this.beanManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ViewConfigDescriptor))
        {
            return false;
        }

        ViewConfigDescriptor that = (ViewConfigDescriptor) o;

        if (!viewId.equals(that.getViewId()))
        {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return viewId.hashCode();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException
    {
        objectInputStream.defaultReadObject();
    }
}