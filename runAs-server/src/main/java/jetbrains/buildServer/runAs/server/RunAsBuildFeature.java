package jetbrains.buildServer.runAs.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.ssh.ServerSshKeyManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class RunAsBuildFeature extends BuildFeature {

  public static final String FEATURE_TYPE = "runAs-build-feature";

  private final String myEditUrl;

  public RunAsBuildFeature(@NotNull PluginDescriptor descriptor) {
    myEditUrl = descriptor.getPluginResourcesPath("runAsBuildFeature.jsp");
  }

  @NotNull
  @Override
  public String getType() {
    return FEATURE_TYPE;
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
    String keyName = params.get(ServerSshKeyManager.TEAMCITY_SSH_KEY_PROP);
    return "Runs SSH agent with the '" + keyName + "' SSH key";
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
        String keyName = properties.get(ServerSshKeyManager.TEAMCITY_SSH_KEY_PROP);
        if (isEmpty(keyName))
          result.add(new InvalidProperty(ServerSshKeyManager.TEAMCITY_SSH_KEY_PROP, "Please select an SSH key"));
        return result;
      }
    };
  }
}