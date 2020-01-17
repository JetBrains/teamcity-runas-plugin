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

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class ShGenerator implements ResourceGenerator<RunAsParams> {
  public static final String BASH_HEADER = "#!/bin/bash";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private final Converter<String, String> myArgumentConverter;

  public ShGenerator(
    @NotNull final Converter<String, String> argumentConverter) {
    myArgumentConverter = argumentConverter;
  }

  @NotNull
  @Override
  public String create(@NotNull final RunAsParams settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(BASH_HEADER);
    sb.append(LINE_SEPARATOR);

    boolean first = true;
    for (CommandLineArgument arg: settings.getCommandLineArguments()) {
      if(first) {
        first = false;
      }
      else {
        sb.append(' ');
      }

      sb.append(myArgumentConverter.convert(arg.getValue()));
    }

    return sb.toString();
  }
}
