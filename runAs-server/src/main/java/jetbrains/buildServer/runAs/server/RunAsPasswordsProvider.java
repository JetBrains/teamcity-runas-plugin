/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
