package jetbrains.buildServer.runAs.server;

import java.util.*;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class RunAsBuildFeature extends BuildFeature {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  @SuppressWarnings("SpellCheckingInspection")
  private static final String DEFAULT_NONINHERITABLE_ENVIRONMENT_VARIABLES
    = "APPDATA"
      + LINE_SEPARATOR + "HOMEPATH"
      + LINE_SEPARATOR + "LOCALAPPDATA"
      + LINE_SEPARATOR + "USERDOMAIN"
      + LINE_SEPARATOR + "USERDOMAIN_ROAMINGPROFILE"
      + LINE_SEPARATOR + "USERNAME"
      + LINE_SEPARATOR + "USERPROFILE";
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

  @Nullable
  @Override
  public Map<String, String> getDefaultParameters() {
    Map<String, String> def = new HashMap<String, String>();
    def.put(Constants.NONINHERITABLE_ENVIRONMENT_VARIABLES, DEFAULT_NONINHERITABLE_ENVIRONMENT_VARIABLES);
    return def;
  }
}