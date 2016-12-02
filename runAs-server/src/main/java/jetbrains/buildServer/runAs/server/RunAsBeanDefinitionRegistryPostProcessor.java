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
      registerBeanDefinition(beanDefinitionRegistry, RunAsRunTypeExtension.class.getName());
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
