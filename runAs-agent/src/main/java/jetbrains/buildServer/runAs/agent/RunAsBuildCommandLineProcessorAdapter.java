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

import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

public class RunAsBuildCommandLineProcessorAdapter extends BuildCommandLineProcessorAdapterImpl implements PositionAware {

  public RunAsBuildCommandLineProcessorAdapter(
      @NotNull final CommandLineSetupBuilder setupBuilder,
      @NotNull final BuildRunnerContextProvider contextProvider,
      @NotNull final AgentEventDispatcher eventDispatcher,
      @NotNull final ExceptionHandler exceptionHandler,
      @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    super(setupBuilder, contextProvider, eventDispatcher, exceptionHandler, commandLineArgumentsService);
  }

  @NotNull
  @Override
  public String getOrderId() {
    return "";
  }

  @NotNull
  @Override
  public PositionConstraint getConstraint() {
    return PositionConstraint.last();
  }
}
