/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.runAs.server;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

public class RunAsBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
  private RunAsConfiguration myRunAsConfiguration;

  public RunAsBeanDefinitionRegistryPostProcessor(@NotNull final RunAsConfiguration runAsConfiguration) {
    myRunAsConfiguration = runAsConfiguration;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
    if(myRunAsConfiguration.getIsUiSupported()) {
      registerBeanDefinition(beanDefinitionRegistry, RunAsBuildFeature.class.getName());
      if(myRunAsConfiguration.getIsUiForBuildStepsSupported()) {
        registerBeanDefinition(beanDefinitionRegistry, RunAsRunTypeExtension.class.getName());
      }
    }
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
  }

  private void registerBeanDefinition(@NotNull final BeanDefinitionRegistry beanDefinitionRegistry, @NotNull final String className)
  {
    final RootBeanDefinition beanDefinition = new RootBeanDefinition(RunAsBuildFeature.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setAbstract(false);
    beanDefinition.setAutowireCandidate(true);
    beanDefinitionRegistry.registerBeanDefinition(className, new RootBeanDefinition(className));
  }
}
