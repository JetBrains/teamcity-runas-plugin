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

package jetbrains.buildServer.runAs.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;

public class RunAsBean {
  public static final RunAsBean Shared = new RunAsBean();

  @NotNull
  public String getRunAsUserKey() {
    return Constants.USER;
  }

  @NotNull
  public String getRunAsPasswordKey() {
    return Constants.PASSWORD;
  }

  @NotNull
  public String getAdditionalCommandLineParametersKey() {
    return Constants.ADDITIONAL_ARGS;
  }

  @NotNull
  public String getWindowsIntegrityLevelKey() {
    return Constants.WINDOWS_INTEGRITY_LEVEL;
  }

  @NotNull
  public List<WindowsIntegrityLevel> getWindowsIntegrityLevels() {
    return Arrays.asList(WindowsIntegrityLevel.values());
  }

  @NotNull
  public String getWindowsLoggingLevelKey() {
    return Constants.LOGGING_LEVEL;
  }

  @NotNull
  public List<LoggingLevel> getLoggingLevels() {
    return Arrays.asList(LoggingLevel.values());
  }
}
