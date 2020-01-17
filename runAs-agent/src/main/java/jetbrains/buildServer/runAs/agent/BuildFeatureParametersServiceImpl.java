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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildFeatureParametersServiceImpl implements BuildFeatureParametersService {
  private final BuildRunnerContextProvider myContextProvider;

  public BuildFeatureParametersServiceImpl(
    BuildRunnerContextProvider contextProvider) {
    myContextProvider = contextProvider;
  }

  @Nullable
  @Override
  public String tryGetBuildFeatureParameter(@NotNull final String buildFeatureType, @NotNull final String parameterName) {
    final List<String> params = new ArrayList<String>();
    for(AgentBuildFeature buildFeature: myContextProvider.getContext().getBuild().getBuildFeaturesOfType(buildFeatureType))
    {
      if (!buildFeatureType.equalsIgnoreCase(buildFeature.getType()))
      {
        continue;
      }

      final Map<String, String> allParams = buildFeature.getParameters();
      return allParams.get(parameterName);
    }

    return null;
  }
}
