package jetbrains.buildServer.runAs.server;

import java.util.*;
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
  public Collection<Parameter> getPasswordParameters(@NotNull final SBuild sBuild) {
    final ArrayList<Parameter> passwords = new ArrayList<Parameter>();

    final SRunningBuild build = sBuild.getAgent().getRunningBuild();

    if(myRunAsConfiguration.getIsUiSupported()) {
      if (build != null) {
        final SBuildType buildType = build.getBuildType();
        if (buildType != null) {
          for (SBuildRunnerDescriptor runner : buildType.getBuildRunners()) {
            final String password = runner.getParameters().get(Constants.PASSWORD_FROM_UI);
            if (!StringUtil.isEmpty(password)) {
              passwords.add(new SimpleParameter(Constants.PASSWORD_FROM_UI + "_" + runner.getId(), password));
            }
          }
        }
      }

      for (SBuildFeatureDescriptor buildFeature : sBuild.getBuildFeaturesOfType(Constants.BUILD_FEATURE_TYPE)) {
        if (!Constants.BUILD_FEATURE_TYPE.equalsIgnoreCase(buildFeature.getType())) {
          continue;
        }

        final Map<String, String> params = buildFeature.getParameters();
        if (!params.containsKey(Constants.PASSWORD_FROM_UI)) {
          continue;
        }

        final String password = params.get(Constants.PASSWORD_FROM_UI);
        if (!StringUtil.isEmpty(password)) {
          passwords.add(new SimpleParameter(Constants.PASSWORD_FROM_UI + "_" + buildFeature.getId(), password));
        }
      }
    }
    else {
      if (build != null) {
        final SBuildAgent agent = build.getAgent();
        final String password = agent.getConfigurationParameters().get(Constants.PASSWORD_FROM_UI);
        if (!StringUtil.isEmpty(password)) {
          passwords.add(new SimpleParameter(Constants.PASSWORD_FROM_UI + "_" + agent.getId(), password));
        }
      }
    }

    final Map<String, String> buildParams = sBuild.getBuildOwnParameters();
    final String password = buildParams.get(Constants.PASSWORD_FROM_UI);
    if(!StringUtil.isEmpty(password)) {
      passwords.add(new SimpleParameter(Constants.PASSWORD_FROM_UI, password));
    }

    return passwords;
  }
}
