package jetbrains.buildServer.runAs.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class RunAsBuildFeature extends BuildFeature {
  private final String myEditUrl;
  private final RunAsBean myBean;

  public RunAsBuildFeature(
    @NotNull final RunAsBean bean,
    @NotNull final PluginDescriptor descriptor) {
    myBean = bean;
    myEditUrl = descriptor.getPluginResourcesPath("runAsBuildFeature.jsp");
  }

  @NotNull
  @Override
  public String getType() {
    return Constants.BUILD_FEATURE_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Run As";
  }

  @Nullable
  @Override
  public String getEditParametersUrl() {
    return myEditUrl;
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> params) {
    final String userName = params.get(myBean.getRunAsUserKey());
    return "Run build steps as \"" + userName + "\"";
  }

  @Override
  public boolean isMultipleFeaturesPerBuildTypeAllowed() {
    return false;
  }

  @Nullable
  @Override
  public PropertiesProcessor getParametersProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        List<InvalidProperty> result = new ArrayList<InvalidProperty>();

        final String userName = properties.get(myBean.getRunAsUserKey());
        if (isEmpty(userName))
          result.add(new InvalidProperty(myBean.getRunAsUserKey(), "Please specify an user name"));

        final String password = properties.get(myBean.getRunAsPasswordKey());
        if (isEmpty(password))
          result.add(new InvalidProperty(myBean.getRunAsPasswordKey(), "Please specify a password"));

        return result;
      }
    };
  }
}