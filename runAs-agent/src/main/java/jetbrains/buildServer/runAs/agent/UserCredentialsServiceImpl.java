package jetbrains.buildServer.runAs.agent;

import java.io.File;
import jetbrains.buildServer.agent.BuildAgentConfigurationEx;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.RunAsMode;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserCredentialsServiceImpl implements UserCredentialsService {
  private final RunnerParametersService myRunnerParametersService;
  private final ParametersService myParametersService;
  private final PropertiesService myPropertiesService;
  private final FileService myFileService;
  private final BuildAgentConfigurationEx myBuildAgentConfiguration;
  private final CommandLineArgumentsService myCommandLineArgumentsService;

  public UserCredentialsServiceImpl(
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final ParametersService parametersService,
    @NotNull final PropertiesService propertiesService,
    @NotNull final FileService fileService,
    @NotNull final BuildAgentConfigurationEx buildAgentConfiguration,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    myRunnerParametersService = runnerParametersService;
    myParametersService = parametersService;
    myPropertiesService = propertiesService;
    myFileService = fileService;
    myBuildAgentConfiguration = buildAgentConfiguration;
    myCommandLineArgumentsService = commandLineArgumentsService;
  }

  @Nullable
  @Override
  public UserCredentials tryGetUserCredentials() {
    UserCredentials userCredentials = null;

    final RunAsMode runAsMode = RunAsMode.tryParse(myRunnerParametersService.tryGetConfigParameter(Constants.RUN_AS_MODE_VAR));
    switch (runAsMode) {
      case CustomCredentials: {
        String credentialsRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_VAR);
        if (!StringUtil.isEmptyOrSpaces(credentialsRef)) {
          throw new BuildStartException("The usage of credentials is prohibited");
        }

        return tryGetCustomCredentials();
      }

      case PredefinedCredentials: {
        String credentialsRef = myRunnerParametersService.tryGetConfigParameter(Constants.CREDENTIALS_VAR);
        if (StringUtil.isEmptyOrSpaces(credentialsRef)) {
          throw new BuildStartException("Configuration parameter \"" + Constants.CREDENTIALS_VAR + "\" was not defined");
        }

        return getPredefinedCredentials(credentialsRef);
      }

      case Enabled: {
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

    throw new BuildStartException("Unknown credentials mode \"" + runAsMode.getDescription() + "\"");
  }

  @Nullable
  private UserCredentials tryGetCustomCredentials() {
    final String userName = myParametersService.tryGetParameter(Constants.USER_VAR);
    final String password = myParametersService.tryGetParameter(Constants.PASSWORD_VAR);

    if(StringUtil.isEmptyOrSpaces(userName) || StringUtil.isEmptyOrSpaces(password)) {
      return null;
    }

    return createCredentials(userName, password, false);
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

    return createCredentials(userName, password, true);
  }

  @NotNull
  private UserCredentials createCredentials(@NotNull final String userName, @NotNull final String password, boolean isPredefined)
  {
    // Get parameters
    final WindowsIntegrityLevel windowsIntegrityLevel = WindowsIntegrityLevel.tryParse(getParam(Constants.WINDOWS_INTEGRITY_LEVEL_VAR, isPredefined));
    final LoggingLevel windowsLoggingLevel = LoggingLevel.tryParse(getParam(Constants.WINDOWS_LOGGING_LEVEL_VAR, isPredefined));

    String additionalArgs = getParam(Constants.ADDITIONAL_ARGS_VAR, isPredefined);
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    return new UserCredentials(userName, password, windowsIntegrityLevel, windowsLoggingLevel, myCommandLineArgumentsService.parseCommandLineArguments(additionalArgs));
  }

  @Nullable
  private String getParam(@NotNull final String paramName, boolean isPredefined) {
    if(isPredefined) {
      return myPropertiesService.tryGetProperty(paramName);
    }

    return myParametersService.tryGetParameter(paramName);
  }
}
