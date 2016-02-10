package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.messages.serviceMessages.Message;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;

public class RunAsSetupBuilder implements CommandLineSetupBuilder {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  static final String TOOL_FILE_NAME = "runAs.cmd";
  static final String CREDENTIALS_EXT = ".cred";
  static final String ARGS_EXT = ".args";
  static final String MESSAGES_EXT = ".messages";
  static final String WARNING_STATUS = "WARNING";
  static final String CONFIG_FILE_CMD_KEY = "-c:";

  private static final String CONFIGURATION_PARAMETER_WAS_NOT_DEFINED_WARNING = "the configuration parameter \"%s\" was not defined or empty";
  private static final String RUN_AS_WAS_NOT_USED_MESSAGE = "RunAs was not used because %s";

  private final RunnerParametersService myParametersService;
  private final FileService myFileService;
  private final ResourcePublisher mySettingsPublisher;
  private final ResourceGenerator<CredentialsSettings> myCredentialsGenerator;
  private final ResourceGenerator<RunAsArgsSettings> myRunAsArgsGenerator;
  private final ResourceGenerator<RunAsArgsSettings> myTeamCityServiceMessagesGenerator;
  private final LoggerService myLoggerService;
  private final CommandLineArgumentsService myCommandLineArgumentsService;

  public RunAsSetupBuilder(
    @NotNull final RunnerParametersService parametersService,
    @NotNull final FileService fileService,
    @NotNull final ResourcePublisher settingsPublisher,
    @NotNull final ResourceGenerator<CredentialsSettings> credentialsGenerator,
    @NotNull final ResourceGenerator<RunAsArgsSettings> runAsArgsGenerator,
    @NotNull final ResourceGenerator<RunAsArgsSettings> teamCityServiceMessagesGenerator,
    @NotNull final LoggerService loggerService,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    myParametersService = parametersService;
    myFileService = fileService;
    mySettingsPublisher = settingsPublisher;
    myCredentialsGenerator = credentialsGenerator;
    myRunAsArgsGenerator = runAsArgsGenerator;
    myTeamCityServiceMessagesGenerator = teamCityServiceMessagesGenerator;
    myLoggerService = loggerService;
    myCommandLineArgumentsService = commandLineArgumentsService;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    if(!myParametersService.isRunningUnderWindows()) {
      return Collections.singleton(commandLineSetup);
    }

    final String user = myParametersService.tryGetConfigParameter(Constants.USER_VAR);
    if(StringUtil.isEmptyOrSpaces(user)) {
      sendWarning(String.format(CONFIGURATION_PARAMETER_WAS_NOT_DEFINED_WARNING, Constants.USER_VAR));
      return Collections.singleton(commandLineSetup);
    }

    final String password = myParametersService.tryGetConfigParameter(Constants.PASSWORD_VAR);
    if(StringUtil.isEmptyOrSpaces(password)) {
      sendWarning(String.format(CONFIGURATION_PARAMETER_WAS_NOT_DEFINED_WARNING, Constants.PASSWORD_VAR));
      return Collections.singleton(commandLineSetup);
    }

    final ArrayList<CommandLineResource> resources = new ArrayList<CommandLineResource>();
    resources.addAll(commandLineSetup.getResources());

    final CredentialsSettings credentialsSettings = new CredentialsSettings(user, password);

    List<CommandLineArgument> cmdLineArgs = new ArrayList<CommandLineArgument>();
    cmdLineArgs.add(new CommandLineArgument(commandLineSetup.getToolPath(), CommandLineArgument.Type.PARAMETER));
    cmdLineArgs.addAll(commandLineSetup.getArgs());
    final String commandLine = myCommandLineArgumentsService.createCommandLineString(cmdLineArgs);
    final String workingDirectory = myFileService.getCheckoutDirectory().getAbsolutePath();
    final RunAsArgsSettings runAsArgsSettings = new RunAsArgsSettings(commandLine, workingDirectory);

    // Credentials
    final File credentialsFile = myFileService.getTempFileName(CREDENTIALS_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, credentialsFile, myCredentialsGenerator.create(credentialsSettings)));

    // RunAs args
    final File runAsArgsFile = myFileService.getTempFileName(ARGS_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, runAsArgsFile, myRunAsArgsGenerator.create(runAsArgsSettings)));

    // Message
    final File messageFile = myFileService.getTempFileName(MESSAGES_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, messageFile, myTeamCityServiceMessagesGenerator.create(runAsArgsSettings)));

    final File tempDirectory = myFileService.getTempDirectory();

    return Collections.singleton(
      new CommandLineSetup(
        getTool().getAbsolutePath(),
        Arrays.asList(
          new CommandLineArgument(tempDirectory.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(CONFIG_FILE_CMD_KEY + myFileService.getRelativePath(tempDirectory, credentialsFile).getPath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(CONFIG_FILE_CMD_KEY + myFileService.getRelativePath(tempDirectory, runAsArgsFile).getPath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(myFileService.getRelativePath(tempDirectory, messageFile).getPath(), CommandLineArgument.Type.PARAMETER)),
        resources));
  }

  private void sendWarning(@NotNull final String reason) {
    myLoggerService.onMessage(new Message(String.format(RUN_AS_WAS_NOT_USED_MESSAGE, reason), WARNING_STATUS, null));
  }

  private File getTool() {
    final File path = new File(myParametersService.getToolPath(Constants.RUN_AS_TOOL_NAME), TOOL_FILE_NAME);
    myFileService.validatePath(path);
    return path;
  }
}