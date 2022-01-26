/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.TeamCityProperties;

public class RunAsConfiguration {
  public boolean getIsUiSupported() {
    return TeamCityProperties.getBooleanOrTrue(Constants.RUN_AS_UI_ENABLED);
  }

  public boolean getIsUiForBuildStepsSupported() {
    return TeamCityProperties.getBoolean(Constants.RUN_AS_UI_STEPS_ENABLED);
  }

  public boolean getIsAclConfiguringSupported() {
    return TeamCityProperties.getBoolean(Constants.RUN_AS_ACL_DEFAULTS_ENABLED);
  }
}