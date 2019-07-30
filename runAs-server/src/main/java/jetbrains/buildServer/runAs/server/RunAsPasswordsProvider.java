package jetbrains.buildServer.runAs.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class RunAsPasswordsProvider implements PasswordsProvider {
  private final RunAsConfiguration myRunAsConfiguration;

  public RunAsPasswordsProvider(@NotNull final RunAsConfiguration runAsConfiguration) {
    myRunAsConfiguration = runAsConfiguration;
  }

  @NotNull
  @Override
  public Collection<Parameter> getPasswordParameters(@NotNull final SBuild build) {
    final ArrayList<Parameter> passwords = new ArrayList<Parameter>();


    if (myRunAsConfiguration.getIsUiSupported()) {
      final SBuildType buildType = build.getBuildType();
      if (buildType != null) {
        for (SBuildRunnerDescriptor runner : buildType.getBuildRunners()) {
          final String password = runner.getParameters().get(Constants.PASSWORD);
          if (!StringUtil.isEmpty(password)) {
            passwords.add(new SimpleParameter(Constants.PASSWORD + "_" + runner.getId(), password));
          }
        }
      }

      for (SBuildFeatureDescriptor buildFeature : build.getBuildFeaturesOfType(Constants.BUILD_FEATURE_TYPE)) {
        if (!Constants.BUILD_FEATURE_TYPE.equalsIgnoreCase(buildFeature.getType())) {
          continue;
        }

        final Map<String, String> params = buildFeature.getParameters();
        if (!params.containsKey(Constants.PASSWORD)) {
          continue;
        }

        final String password = params.get(Constants.PASSWORD);
        if (!StringUtil.isEmpty(password)) {
          passwords.add(new SimpleParameter(Constants.PASSWORD + "_" + buildFeature.getId(), password));
        }
      }
    } else {
      final SBuildAgent agent = build.getAgent();
      final String password = agent.getConfigurationParameters().get(Constants.PASSWORD);
      if (!StringUtil.isEmpty(password)) {
        passwords.add(new SimpleParameter(Constants.PASSWORD + "_" + agent.getId(), password));
      }
    }

    final Map<String, String> buildParams = build.getBuildOwnParameters();
    final String password = buildParams.get(Constants.PASSWORD);
    if(!StringUtil.isEmpty(password)) {
      passwords.add(new SimpleParameter(Constants.PASSWORD, password));
    }

    return passwords;
  }
}
