package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class RunAsSetupBuilder implements CommandLineSetupBuilder {
  static final String TOOL_FILE_NAME = "runAs.cmd";
  static final String CREDENTIALS_EXT = ".args";
  static final String CMD_EXT = ".cmd";
  private final RunnerParametersService myParametersService;
  private final BuildFeatureParametersService myBuildFeatureParametersService;
  private final FileService myFileService;
  private final ResourcePublisher mySettingsPublisher;
  private final ResourceGenerator<Settings> mySettingsGenerator;
  private final ResourceGenerator<RunAsCmdSettings> myRunAsCmdGenerator;
  private final CommandLineArgumentsService myCommandLineArgumentsService;

  public RunAsSetupBuilder(
    @NotNull final RunnerParametersService parametersService,
    @NotNull final BuildFeatureParametersService buildFeatureParametersService,
    @NotNull final FileService fileService,
    @NotNull final ResourcePublisher settingsPublisher,
    @NotNull final ResourceGenerator<Settings> settingsGenerator,
    @NotNull final ResourceGenerator<RunAsCmdSettings> runAsCmdGenerator,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    myParametersService = parametersService;
    myBuildFeatureParametersService = buildFeatureParametersService;
    myFileService = fileService;
    mySettingsPublisher = settingsPublisher;
    mySettingsGenerator = settingsGenerator;
    myRunAsCmdGenerator = runAsCmdGenerator;
    myCommandLineArgumentsService = commandLineArgumentsService;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    if(!myParametersService.isRunningUnderWindows()) {
      return Collections.singleton(commandLineSetup);
    }

    // Get parameters
    final List<String> userNames = myBuildFeatureParametersService.getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.USER_VAR);
    final List<String> passwords = myBuildFeatureParametersService.getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.PASSWORD_VAR);
    if(userNames.size() == 0 || passwords.size() == 0) {
      return Collections.singleton(commandLineSetup);
    }

    final String userName = userNames.get(0);
    final String password = passwords.get(0);
    if(StringUtil.isEmptyOrSpaces(userName) || password == null) {
      return Collections.singleton(commandLineSetup);
    }

    String additionalArgs = myParametersService.tryGetConfigParameter(Constants.ADDITIONAL_ARGS_VAR);
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    // Resources
    final ArrayList<CommandLineResource> resources = new ArrayList<CommandLineResource>();
    resources.addAll(commandLineSetup.getResources());

    // Settings
    final File credentialsFile = myFileService.getTempFileName(CREDENTIALS_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, credentialsFile.getAbsoluteFile(), mySettingsGenerator.create(new Settings(userName, password, myCommandLineArgumentsService.parseCommandLineArguments(additionalArgs)))));

    // Command
    List<CommandLineArgument> cmdLineArgs = new ArrayList<CommandLineArgument>();
    cmdLineArgs.add(new CommandLineArgument(commandLineSetup.getToolPath(), CommandLineArgument.Type.PARAMETER));
    cmdLineArgs.addAll(commandLineSetup.getArgs());

    final RunAsCmdSettings runAsCmdSettings = new RunAsCmdSettings(
      myCommandLineArgumentsService.createCommandLineString(cmdLineArgs),
      myFileService.getCheckoutDirectory().getAbsolutePath());

    final File cmdFile = myFileService.getTempFileName(CMD_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, cmdFile.getAbsoluteFile(), myRunAsCmdGenerator.create(runAsCmdSettings)));

    return Collections.singleton(
      new CommandLineSetup(
        getTool().getAbsolutePath(),
        Arrays.asList(
          new CommandLineArgument(credentialsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER)),
        resources));
  }

  private File getTool() {
    final File path = new File(myParametersService.getToolPath(Constants.RUN_AS_TOOL_NAME), TOOL_FILE_NAME);
    myFileService.validatePath(path);
    return path;
  }
}