package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.CredentialsMode;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsProviderImpl implements SettingsProvider {
  private final ParametersService myParametersService;
  private final UserCredentialsService myCredentialsService;
  private final CommandLineArgumentsService myCommandLineArgumentsService;

  public SettingsProviderImpl(
    @NotNull final ParametersService parametersService,
    @NotNull final UserCredentialsService credentialsService,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    myParametersService = parametersService;
    myCredentialsService = credentialsService;
    myCommandLineArgumentsService = commandLineArgumentsService;
  }

  @Nullable
  @Override
  public Settings tryGetSettings() {
    // Get credentials
    final UserCredentials userCredentials = myCredentialsService.tryGetUserCredentials();
    if(userCredentials == null) {
      return null;
    }

    // Get parameters
    final WindowsIntegrityLevel windowsIntegrityLevel = WindowsIntegrityLevel.tryParse(myParametersService.tryGetParameter(Constants.WINDOWS_INTEGRITY_LEVEL_VAR));
    final LoggingLevel windowsLoggingLevel = LoggingLevel.tryParse(myParametersService.tryGetParameter(Constants.WINDOWS_LOGGING_LEVEL_VAR));

    String additionalArgs = myParametersService.tryGetParameter(Constants.ADDITIONAL_ARGS_VAR);
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    return new Settings(userCredentials, windowsIntegrityLevel, windowsLoggingLevel, myCommandLineArgumentsService.parseCommandLineArguments(additionalArgs));
  }
}
