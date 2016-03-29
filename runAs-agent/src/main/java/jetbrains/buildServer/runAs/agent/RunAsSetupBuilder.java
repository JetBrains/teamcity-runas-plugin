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
import org.jetbrains.annotations.Nullable;

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
    final String userName = tryGetParameter(Constants.USER_VAR);
    if(StringUtil.isEmptyOrSpaces(userName)) {
      return Collections.singleton(commandLineSetup);
    }

    String password = tryGetParameter(Constants.PASSWORD_VAR);
    if(password == null) {
      password = "";
    }

    String additionalArgs = tryGetParameter(Constants.ADDITIONAL_ARGS_VAR);
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    // Resources
    final ArrayList<CommandLineResource> resources = new ArrayList<CommandLineResource>();
    resources.addAll(commandLineSetup.getResources());

    // Settings
    final File settingsFile = myFileService.getTempFileName(CREDENTIALS_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, settingsFile.getAbsoluteFile(), mySettingsGenerator.create(new Settings(userName, myCommandLineArgumentsService.parseCommandLineArguments(additionalArgs)))));

    // Command
    List<CommandLineArgument> cmdLineArgs = new ArrayList<CommandLineArgument>();
    cmdLineArgs.add(new CommandLineArgument(commandLineSetup.getToolPath(), CommandLineArgument.Type.PARAMETER));
    cmdLineArgs.addAll(commandLineSetup.getArgs());

    final RunAsCmdSettings runAsCmdSettings = new RunAsCmdSettings(myCommandLineArgumentsService.createCommandLineString(cmdLineArgs));

    final File cmdFile = myFileService.getTempFileName(CMD_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, cmdFile.getAbsoluteFile(), myRunAsCmdGenerator.create(runAsCmdSettings)));

    return Collections.singleton(
      new CommandLineSetup(
        getTool().getAbsolutePath(),
        Arrays.asList(
          new CommandLineArgument(settingsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(password, CommandLineArgument.Type.PARAMETER)),
        resources));
  }

  @Nullable
  private String tryGetParameter(@NotNull final String paramName)
  {
    final List<String> paramValues = myBuildFeatureParametersService.getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, paramName);
    if (paramValues.size() == 0) {
      return myParametersService.tryGetConfigParameter(paramName);
    }

    return paramValues.get(0);
  }

  private File getTool() {
    final File path = new File(myParametersService.getToolPath(Constants.RUN_AS_TOOL_NAME), TOOL_FILE_NAME);
    myFileService.validatePath(path);
    return path;
  }
}