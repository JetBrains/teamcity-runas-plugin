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

import java.util.HashMap;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;

public class UserCredentials {
  @NotNull private final String myProfile;
  @NotNull private final String myUser;
  @NotNull private final String myPassword;
  @NotNull private final WindowsIntegrityLevel myWindowsIntegrityLevel;
  @NotNull private final LoggingLevel myLoggingLevel;
  @NotNull private final List<CommandLineArgument> myAdditionalArgs;

  public UserCredentials(
    @NotNull final String profile,
    @NotNull final String user,
    @NotNull final String password,
    @NotNull final WindowsIntegrityLevel windowsIntegrityLevel,
    @NotNull final LoggingLevel loggingLevel,
    @NotNull final List<CommandLineArgument> additionalArgs) {
    myProfile = profile;
    myUser = user;
    myPassword = password;
    myWindowsIntegrityLevel = windowsIntegrityLevel;
    myLoggingLevel = loggingLevel;
    myAdditionalArgs = additionalArgs;
  }

  @NotNull
  String getProfile() {
    return myProfile;
  }

  @NotNull
  String getUser() {
    return myUser;
  }

  @NotNull
  String getPassword() {
    return myPassword;
  }

  public WindowsIntegrityLevel getWindowsIntegrityLevel() {
    return myWindowsIntegrityLevel;
  }

  public LoggingLevel getLoggingLevel() {
    return myLoggingLevel;
  }

  @NotNull
  List<CommandLineArgument> getAdditionalArgs() {
    return myAdditionalArgs;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof UserCredentials)) return false;

    final UserCredentials that = (UserCredentials)o;

    if (!myProfile.equals(that.myProfile)) return false;
    if (!getUser().equals(that.getUser())) return false;
    if (!getPassword().equals(that.getPassword())) return false;
    if (getWindowsIntegrityLevel() != that.getWindowsIntegrityLevel()) return false;
    if (getLoggingLevel() != that.getLoggingLevel()) return false;
    return getAdditionalArgs().equals(that.getAdditionalArgs());

  }

  @Override
  public int hashCode() {
    int result = myProfile.hashCode();
    result = 31 * result + getUser().hashCode();
    result = 31 * result + getPassword().hashCode();
    result = 31 * result + getWindowsIntegrityLevel().hashCode();
    result = 31 * result + getLoggingLevel().hashCode();
    result = 31 * result + getAdditionalArgs().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "UserCredentials",
      new HashMap<String, Object>() {{
        this.put("Profile", myProfile);
        this.put("User", myUser);
        this.put("WindowsIntegrityLevel", myWindowsIntegrityLevel);
        this.put("LoggingLevel", myLoggingLevel);
        this.put("AdditionalArgs", LogUtils.toString(myAdditionalArgs));
      }});
  }
}
