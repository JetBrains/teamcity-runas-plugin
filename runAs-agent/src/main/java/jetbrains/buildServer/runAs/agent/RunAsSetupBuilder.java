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

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetupBuilder;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

public class RunAsSetupBuilder implements CommandLineSetupBuilder {
  @NotNull private final Environment myEnvironment;
  @NotNull private final CommandLineSetupBuilder myRunAsWindowsSetupBuilder;
  @NotNull private final CommandLineSetupBuilder myRunAsUnixSetupBuilder;
  @NotNull private final CommandLineSetupBuilder myRunAsMacSetupBuilder;

  public RunAsSetupBuilder(
    @NotNull final Environment environment,
    @NotNull final CommandLineSetupBuilder runAsWindowsSetupBuilder,
    @NotNull final CommandLineSetupBuilder runAsUnixSetupBuilder,
    @NotNull final CommandLineSetupBuilder runAsMacSetupBuilder) {
    myEnvironment = environment;
    myRunAsWindowsSetupBuilder = runAsWindowsSetupBuilder;
    myRunAsUnixSetupBuilder = runAsUnixSetupBuilder;
    myRunAsMacSetupBuilder = runAsMacSetupBuilder;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    CommandLineSetupBuilder setupBuilder = myRunAsUnixSetupBuilder;
    switch (myEnvironment.getOperationSystem()) {
      case Windows:
        setupBuilder = myRunAsWindowsSetupBuilder;
        break;

      case Mac:
        setupBuilder = myRunAsMacSetupBuilder;
        break;
    }

    return setupBuilder.build(commandLineSetup);
  }
}
