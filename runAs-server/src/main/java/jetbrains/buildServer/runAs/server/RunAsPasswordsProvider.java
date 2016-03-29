package jetbrains.buildServer.runAs.server;

import java.util.*;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.Parameter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import org.jetbrains.annotations.NotNull;

public class RunAsPasswordsProvider implements PasswordsProvider {
  @NotNull
  @Override
  public Collection<Parameter> getPasswordParameters(@NotNull final SBuild sBuild) {
    for(SBuildFeatureDescriptor buildFeature: sBuild.getBuildFeaturesOfType(Constants.BUILD_FEATURE_TYPE))
    {
      if (!Constants.BUILD_FEATURE_TYPE.equalsIgnoreCase(buildFeature.getType()))
      {
        continue;
      }

      final Map<String, String> params = buildFeature.getParameters();
      if (!params.containsKey(Constants.PASSWORD_VAR))
      {
        continue;
      }

      return new ArrayList<Parameter>(Arrays.asList(new SimpleParameter(Constants.PASSWORD_VAR, params.get(Constants.PASSWORD_VAR))));
    }

    return Collections.emptyList();
  }
}