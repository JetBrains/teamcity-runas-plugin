package jetbrains.buildServer.runAs.agent;

import java.io.File;
import jetbrains.buildServer.agent.BuildAgentConfigurationEx;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.CredentialsMode;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserCredentialsServiceImpl implements UserCredentialsService {
  private final RunnerParametersService myRunnerParametersService;
  private final ParametersService myParametersService;
  private final PropertiesService myPropertiesService;
  private final FileService myFileService;
  private BuildAgentConfigurationEx myBuildAgentConfiguration;

  public UserCredentialsServiceImpl(
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final ParametersService parametersService,
    @NotNull final PropertiesService propertiesService,
    @NotNull final FileService fileService,
    @NotNull final BuildAgentConfigurationEx buildAgentConfiguration) {
    myRunnerParametersService = runnerParametersService;
    myParametersService = parametersService;
    myPropertiesService = propertiesService;
    myFileService = fileService;
    myBuildAgentConfiguration = buildAgentConfiguration;
  }

  @Nullable
  @Override
  public UserCredentials tryGetUserCredentials() {
    UserCredentials userCredentials = null;

    final CredentialsMode credentialsMode = CredentialsMode.tryParse(myRunnerParametersService.tryGetConfigParameter(Constants.CREDENTIALS_MODE_VAR));
    switch (credentialsMode) {
      case Prohibited: {
        String credentialsRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_VAR);
        if (!StringUtil.isEmptyOrSpaces(credentialsRef)) {
          throw new BuildStartException("The usage of credentials is prohibited");
        }

        return tryGetCustomCredentials();
      }

      case Enforced: {
        String credentialsRef = myRunnerParametersService.tryGetConfigParameter(Constants.CREDENTIALS_VAR);
        if (StringUtil.isEmptyOrSpaces(credentialsRef)) {
          throw new BuildStartException("Configuration parameter \"" + Constants.CREDENTIALS_VAR + "\" was not defined");
        }

        return getPredefinedCredentials(credentialsRef);
      }

      case Allowed: {
        userCredentials = tryGetCustomCredentials();
        if(userCredentials != null) {
          return userCredentials;
        }

        String credentialsRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_VAR);
        if (!StringUtil.isEmptyOrSpaces(credentialsRef)) {
          return getPredefinedCredentials(credentialsRef);
        }

        return null;
      }

      case Disabled: {
        return null;
      }
    }

    throw new BuildStartException("Unknown credentials mode \"" + credentialsMode.getDescription() + "\"");
  }

  private UserCredentials tryGetCustomCredentials() {
    return tryCreateCredentials(myParametersService.tryGetParameter(Constants.USER_VAR), myParametersService.tryGetParameter(Constants.PASSWORD_VAR));
  }

  @NotNull
  private UserCredentials getPredefinedCredentials(@NotNull final String credentials) {
    final String userName;
    final String password;

    final String credentialsDirectoryStr = myRunnerParametersService.tryGetConfigParameter(Constants.CREDENTIALS_DIRECTORY_VAR);
    if(credentialsDirectoryStr == null) {
      throw new BuildStartException("Configuration parameter \"" + Constants.CREDENTIALS_DIRECTORY_VAR + "\" was not defined");
    }

    final File credentialsDirectory = new File(myBuildAgentConfiguration.getAgentConfDirectory(), credentialsDirectoryStr);
    if(!myFileService.exists(credentialsDirectory) || !myFileService.isDirectory(credentialsDirectory)) {
      throw new BuildStartException("Credentials directory was not found");
    }

    final File credentialsFile = new File(credentialsDirectory, credentials + ".properties");
    if(!myFileService.exists(credentialsFile) || myFileService.isDirectory(credentialsFile)) {
      throw new BuildStartException("Credentials file for \"" + credentials + "\" was not found");
    }

    myPropertiesService.load(credentialsFile);
    userName = myPropertiesService.tryGetProperty(Constants.USER_VAR);
    if(StringUtil.isEmptyOrSpaces(userName)) {
      throw new BuildStartException("Username must be defined for \"" + credentials + "\"");
    }

    password = myPropertiesService.tryGetProperty(Constants.PASSWORD_VAR);
    if(StringUtil.isEmptyOrSpaces(password)) {
      throw new BuildStartException("Password must be defined for \"" + credentials + "\"");
    }

    return new UserCredentials(userName, password);
  }

  @Nullable
  private UserCredentials tryCreateCredentials(
    @Nullable final String userName,
    @Nullable final String password) {
    if(StringUtil.isEmptyOrSpaces(userName) || StringUtil.isEmptyOrSpaces(password)) {
      return null;
    }

    return new UserCredentials(userName, password);
  }
}
