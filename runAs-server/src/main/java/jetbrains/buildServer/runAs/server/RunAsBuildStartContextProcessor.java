/*
 * Copyright 2000-2021 JetBrains s.r.o.
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
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import org.jetbrains.annotations.NotNull;

public class RunAsBuildStartContextProcessor implements BuildStartContextProcessor {
  private final RunAsConfiguration myRunAsConfiguration;

  public RunAsBuildStartContextProcessor(@NotNull final RunAsConfiguration runAsConfiguration) {
    myRunAsConfiguration = runAsConfiguration;
  }

  @Override
  public void updateParameters(@NotNull final BuildStartContext buildStartContext) {
    buildStartContext.addSharedParameter(Constants.RUN_AS_UI_ENABLED, Boolean.toString(myRunAsConfiguration.getIsUiSupported()));
  }
}
