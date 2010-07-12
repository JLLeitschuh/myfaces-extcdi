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
package org.apache.myfaces.extensions.cdi.javaee.jsf.impl.util;

import org.apache.myfaces.extensions.cdi.core.api.manager.BeanManagerProvider;
import org.apache.myfaces.extensions.cdi.core.api.tools.annotate.DefaultAnnotation;
import org.apache.myfaces.extensions.cdi.core.impl.scope.conversation.spi.WindowContextManager;
import org.apache.myfaces.extensions.cdi.javaee.jsf.api.qualifier.Jsf;
import org.apache.myfaces.extensions.cdi.javaee.jsf.impl.scope.conversation.WindowContextIdHolderComponent;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Gerhard Petracek
 */
public class ConversationUtils
{
    private static final Jsf JSF_QUALIFIER = DefaultAnnotation.of(Jsf.class);

    /**
     * @return the descriptor of a custom
     * {@link org.apache.myfaces.extensions.cdi.core.impl.scope.conversation.spi.WindowContextManager}
     * with the qualifier {@link org.apache.myfaces.extensions.cdi.javaee.jsf.api.qualifier.Jsf} or
     *         the descriptor of the default implementation provided by this module
     */
    public static Bean<WindowContextManager> resolveConversationManagerBean()
    {
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();

        Set<?> conversationManagerBeans = beanManager.getBeans(WindowContextManager.class, JSF_QUALIFIER);

        if (conversationManagerBeans.isEmpty())
        {
            conversationManagerBeans = getDefaultConversationManager(beanManager);
        }

        if (conversationManagerBeans.size() != 1)
        {
            throw new IllegalStateException(conversationManagerBeans.size() + " conversation-managers were found");
        }
        //noinspection unchecked
        return (Bean<WindowContextManager>) conversationManagerBeans.iterator().next();
    }

    /**
     * @param beanManager current {@link javax.enterprise.inject.spi.BeanManager}
     * @return the descriptor of the default
     * {@link org.apache.myfaces.extensions.cdi.core.impl.scope.conversation.spi.WindowContextManager}
     */
    private static Set<Bean<?>> getDefaultConversationManager(BeanManager beanManager)
    {
        return beanManager.getBeans(WindowContextManager.class);
    }

    public static Long resolveWindowContextId(boolean requestParameterSupported)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();

        String idViaGetRequest = null;

        if (requestParameterSupported)
        {
            idViaGetRequest = (String) requestParameterMap
                    .get(WindowContextManager.WINDOW_CONTEXT_ID_PARAMETER_KEY);
        }

        Long id = null;
        if (idViaGetRequest != null)
        {
            try
            {
                id = Long.parseLong(idViaGetRequest);
            }
            catch (NumberFormatException e)
            {
                id = null;
            }
        }

        if (id == null)
        {
            Map requestMap = facesContext.getExternalContext().getRequestMap();

            id = (Long) requestMap.get(WindowContextManager.WINDOW_CONTEXT_ID_PARAMETER_KEY);
        }

        if (id != null)
        {
            return id;
        }

        WindowContextIdHolderComponent windowContextIdHolder = getWindowContextIdHolderComponent(facesContext);

        if (windowContextIdHolder != null)
        {
            return windowContextIdHolder.getWindowContextId();
        }

        return null;
    }

    public static WindowContextIdHolderComponent getWindowContextIdHolderComponent(FacesContext facesContext)
    {
        List<UIComponent> uiComponents = facesContext.getViewRoot().getChildren();
        for (UIComponent uiComponent : uiComponents)
        {
            if (uiComponent instanceof WindowContextIdHolderComponent)
            {
                return ((WindowContextIdHolderComponent) uiComponent);
            }
        }

        return null;
    }
}