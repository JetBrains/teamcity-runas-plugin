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

package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParametersServiceImpl implements ParametersService {
  private final RunnerParametersService myRunnerParametersService;
  private final BuildFeatureParametersService myBuildFeatureParametersService;

  public ParametersServiceImpl(
      @NotNull final RunnerParametersService runnerParametersService,
      @NotNull final BuildFeatureParametersService buildFeatureParametersService) {
    myRunnerParametersService = runnerParametersService;
    myBuildFeatureParametersService = buildFeatureParametersService;
  }

  @Nullable
  @Override
  public String tryGetParameter(@NotNull final String paramName) {
    final String isRunAsUiEnabledStr = myRunnerParametersService.tryGetConfigParameter(Constants.RUN_AS_UI_ENABLED);
    final boolean isRunAsUiEnabled = StringUtil.isEmpty(isRunAsUiEnabledStr) || !Boolean.toString(false).equalsIgnoreCase(isRunAsUiEnabledStr);
    if(isRunAsUiEnabled) {
      String paramValue = myRunnerParametersService.tryGetRunnerParameter(paramName);
      if (!StringUtil.isEmptyOrSpaces(paramValue)) {
        return paramValue;
      }

      paramValue = myBuildFeatureParametersService.tryGetBuildFeatureParameter(Constants.BUILD_FEATURE_TYPE, paramName);
      if (!StringUtil.isEmptyOrSpaces(paramValue)) {
        return paramValue;
      }
    }

    return tryGetConfigParameter(paramName);
  }

  @Override
  public String tryGetConfigParameter(@NotNull final String configParamName) {
    return myRunnerParametersService.tryGetConfigParameter(configParamName);
  }
}
