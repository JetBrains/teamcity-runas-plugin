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

package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import org.jetbrains.annotations.NotNull;

public class RunAsParams {
  private final List myCommandLineArguments;

  public RunAsParams(
    @NotNull final List<CommandLineArgument> commandLineArguments) {

    myCommandLineArguments = commandLineArguments;
  }

  public List<CommandLineArgument> getCommandLineArguments() {
    return myCommandLineArguments;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof RunAsParams)) return false;

    final RunAsParams that = (RunAsParams)o;

    return getCommandLineArguments().equals(that.getCommandLineArguments());

  }

  @Override
  public int hashCode() {
    return getCommandLineArguments().hashCode();
  }
}
