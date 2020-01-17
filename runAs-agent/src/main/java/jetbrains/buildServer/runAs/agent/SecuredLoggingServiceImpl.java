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

import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;

public class SecuredLoggingServiceImpl implements SecuredLoggingService {
  static final String TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE = "teamcity.buildLog.logCommandLine";
  private final BuildRunnerContextProvider myContextProvider;

  public SecuredLoggingServiceImpl(
    @NotNull final BuildRunnerContextProvider contextProvider) {
    myContextProvider = contextProvider;
  }

  @Override
  public void disableLoggingOfCommandLine()
  {
    myContextProvider.getContext().addConfigParameter(TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE, Boolean.toString(false));
  }
}
