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

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

public class WindowsSettingsGenerator implements ResourceGenerator<UserCredentials> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "-u:";
  private static final String INTEGRITY_LEVEL_CMD_KEY = "-il:";
  private static final String LOGGING_LEVEL_CMD_KEY = "-l:";

  @NotNull
  @Override
  public String create(@NotNull final UserCredentials userCredentials) {
    final StringBuilder sb = new StringBuilder();

    final String user = userCredentials.getUser();
    if(!StringUtil.isEmptyOrSpaces(user)) {
      sb.append(USER_CMD_KEY);
      sb.append(userCredentials.getUser());
    }

    if(userCredentials.getWindowsIntegrityLevel() != WindowsIntegrityLevel.Auto) {
      if(sb.length() > 0)
      {
        sb.append(LINE_SEPARATOR);
      }

      sb.append(INTEGRITY_LEVEL_CMD_KEY);
      sb.append(userCredentials.getWindowsIntegrityLevel().getValue());
    }

    if(userCredentials.getLoggingLevel() != LoggingLevel.Off) {
      if(sb.length() > 0)
      {
        sb.append(LINE_SEPARATOR);
      }

      sb.append(LOGGING_LEVEL_CMD_KEY);
      sb.append(userCredentials.getLoggingLevel().getValue());
    }

    if(sb.length() > 0 && userCredentials.getAdditionalArgs().size() > 0) {
      sb.append(LINE_SEPARATOR);
    }

    sb.append(
      StringUtil.join(
        CollectionsUtil.convertCollection(
          userCredentials.getAdditionalArgs(),
          new Converter<String, CommandLineArgument>() {
            @Override
            public String createFrom(@NotNull final CommandLineArgument commandLineArgument) {
              return commandLineArgument.getValue();
            }
        }),
        LINE_SEPARATOR));

    return sb.toString();
  }
}