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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserCredentialsServiceImpl implements UserCredentialsService {
  static final String DEFAULT_PROFILE = "default";
  private static final Logger LOG = Logger.getInstance(UserCredentialsServiceImpl.class.getName());
  private final ParametersService myParametersService;
  private final ProfileParametersService myProfileParametersService;
  private final CommandLineArgumentsService myCommandLineArgumentsService;

  public UserCredentialsServiceImpl(
    @NotNull final ParametersService parametersService,
    @NotNull final ProfileParametersService profileParametersService,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    myParametersService = parametersService;
    myProfileParametersService = profileParametersService;
    myCommandLineArgumentsService = commandLineArgumentsService;
  }

  @Nullable
  @Override
  public UserCredentials tryGetUserCredentials() {
    final boolean allowCustomCredentials = ParameterUtils.parseBoolean(myParametersService.tryGetConfigParameter(Constants.ALLOW_CUSTOM_CREDENTIALS), true);
    final boolean allowProfileIdFromServer = ParameterUtils.parseBoolean(myParametersService.tryGetConfigParameter(Constants.ALLOW_PROFILE_ID_FROM_SERVER), false);
    if(allowCustomCredentials && allowProfileIdFromServer) {
      String profileRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_PROFILE_ID);
      final boolean profileWasDefined = !StringUtil.isEmptyOrSpaces(profileRef);
      UserCredentials profileCredentials = null;
      if(!profileWasDefined) {
        profileCredentials = getPredefinedProfile(DEFAULT_PROFILE, false);
        if(LOG.isDebugEnabled()) {
          LOG.debug("tryGetUserCredentials predefined \"" + Constants.CREDENTIALS_PROFILE_ID + "\": " + profileCredentials);
        }
      }

      UserCredentials customCredentials = tryGetCustomCredentials();
      if(customCredentials != null) {
        if(profileWasDefined || profileCredentials != null) {
          LOG.info("Build step attempted to use custom credentials while credentials profiles are configured on the agent. Build step has not been executed and build problem was raised.");
          throw new BuildStartException("Build step cannot be executed with custom credentials on this agent. Please contact system administrator.");
        }

        if(LOG.isDebugEnabled()) {
          LOG.debug("tryGetUserCredentials custom: " + customCredentials);
        }

        return customCredentials;
      }

      if (!profileWasDefined) {
        return profileCredentials;
      }

      profileCredentials = getPredefinedProfile(profileRef, true);
      if(LOG.isDebugEnabled()) {
        LOG.debug("tryGetUserCredentials predefined \"" + profileRef + "\": " + profileCredentials);
      }

      return profileCredentials;
    }

    if(allowCustomCredentials) {
      UserCredentials customCredentials = tryGetCustomCredentials();
      if(LOG.isDebugEnabled()) {
        LOG.debug("tryGetUserCredentials custom: " + customCredentials);
      }

      return customCredentials;
    }

    if(allowProfileIdFromServer) {
      String credentialsRef = myParametersService.tryGetConfigParameter(Constants.CREDENTIALS_PROFILE_ID);
      if (StringUtil.isEmptyOrSpaces(credentialsRef)) {
        credentialsRef = DEFAULT_PROFILE;
      }

      UserCredentials profileCredentials = getPredefinedProfile(credentialsRef, true);
      if(LOG.isDebugEnabled()) {
        LOG.debug("tryGetUserCredentials predefined \"" + credentialsRef + "\": " + profileCredentials);
      }

      return profileCredentials;
    }

    LOG.debug("tryGetUserCredentials returns null");
    return null;
  }

  @Nullable
  private UserCredentials tryGetCustomCredentials() {
    final String userName = tryGetFirstNotEmpty(myParametersService.tryGetParameter(Constants.USER));
    final String password = tryGetFirstNotEmpty(myParametersService.tryGetParameter(Constants.PASSWORD), myParametersService.tryGetParameter(Constants.CONFIG_PASSWORD));

    if(StringUtil.isEmptyOrSpaces(userName) || StringUtil.isEmptyOrSpaces(password)) {
      return null;
    }

    return createCredentials("", userName, password, false);
  }

  @Nullable
  private UserCredentials getPredefinedProfile(@NotNull final String profileName, final boolean trowException) {
    final String userName;
    final String password;

    userName = tryGetFirstNotEmpty(myProfileParametersService.tryGetProperty(profileName, Constants.USER));
    if(StringUtil.isEmptyOrSpaces(userName)) {
      if(trowException) {
        throw new BuildStartException("RunAs user must be defined for \"" + profileName + "\"");
      }
      else {
        return null;
      }
    }

    password = tryGetFirstNotEmpty(myProfileParametersService.tryGetProperty(profileName, Constants.PASSWORD), myProfileParametersService.tryGetProperty(profileName, Constants.CONFIG_PASSWORD));
    if(StringUtil.isEmptyOrSpaces(password)) {
      if(trowException) {
        throw new BuildStartException("RunAs password must be defined for \"" + profileName + "\"");
      }
      else {
        return null;
      }
    }

    return createCredentials(profileName, userName, password, true);
  }

  @NotNull
  private UserCredentials createCredentials(@NotNull final String profileName, @NotNull final String userName, @NotNull final String password, boolean isPredefined)
  {
    // Get parameters
    final WindowsIntegrityLevel windowsIntegrityLevel = WindowsIntegrityLevel.tryParse(getParam(profileName, Constants.WINDOWS_INTEGRITY_LEVEL, isPredefined));
    final LoggingLevel loggingLevel = LoggingLevel.tryParse(getParam(profileName, Constants.LOGGING_LEVEL, isPredefined));

    String additionalArgs = tryGetFirstNotEmpty(getParam(profileName, Constants.ADDITIONAL_ARGS, isPredefined));
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    return new UserCredentials(
      profileName,
      userName,
      password,
      windowsIntegrityLevel,
      loggingLevel,
      myCommandLineArgumentsService.parseCommandLineArguments(additionalArgs));
  }

  @Nullable
  private String getParam(@Nullable final String credentials, @NotNull final String paramName, boolean isPredefined) {
    if(isPredefined && credentials != null) {
      return myProfileParametersService.tryGetProperty(credentials, paramName);
    }

    return myParametersService.tryGetParameter(paramName);
  }

  @Nullable
  private String tryGetFirstNotEmpty(String ... values) {
    for(String value: values) {
      if(!StringUtil.isEmptyOrSpaces(value)) {
        return value;
      }
    }

    return null;
  }
}
