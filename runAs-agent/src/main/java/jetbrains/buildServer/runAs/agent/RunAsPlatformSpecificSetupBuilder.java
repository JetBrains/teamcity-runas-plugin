package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;

public class RunAsPlatformSpecificSetupBuilder implements CommandLineSetupBuilder {
  static final String TOOL_FILE_NAME = "runAs";
  static final String ARGS_EXT = ".args";
  private final SettingsProvider mySettingsProvider;
  private final RunnerParametersService myParametersService;
  private final FileService myFileService;
  private final ResourcePublisher mySettingsPublisher;
  private final ResourceGenerator<Settings> mySettingsGenerator;
  private final ResourceGenerator<RunAsCmdSettings> myRunAsCmdGenerator;
  private final CommandLineArgumentsService myCommandLineArgumentsService;
  private final String myCommandFileExtension;

  public RunAsPlatformSpecificSetupBuilder(
    @NotNull final SettingsProvider settingsProvider,
    @NotNull final RunnerParametersService parametersService,
    @NotNull final FileService fileService,
    @NotNull final ResourcePublisher settingsPublisher,
    @NotNull final ResourceGenerator<Settings> settingsGenerator,
    @NotNull final ResourceGenerator<RunAsCmdSettings> runAsCmdGenerator,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService,
    @NotNull final String commandFileExtension) {
    mySettingsProvider = settingsProvider;
    myParametersService = parametersService;
    myFileService = fileService;
    mySettingsPublisher = settingsPublisher;
    mySettingsGenerator = settingsGenerator;
    myRunAsCmdGenerator = runAsCmdGenerator;
    myCommandLineArgumentsService = commandLineArgumentsService;
    myCommandFileExtension = commandFileExtension;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    // Get settings
    final Settings settings = mySettingsProvider.tryGetSettings();
    if(settings == null) {
      return Collections.singleton(commandLineSetup);
    }

    // Resources
    final ArrayList<CommandLineResource> resources = new ArrayList<CommandLineResource>();
    resources.addAll(commandLineSetup.getResources());

    // Settings
    final File settingsFile = myFileService.getTempFileName(ARGS_EXT);
    resources.add(new CommandLineFile(mySettingsPublisher, settingsFile.getAbsoluteFile(), mySettingsGenerator.create(settings)));

    // Command
    List<CommandLineArgument> cmdLineArgs = new ArrayList<CommandLineArgument>();
    cmdLineArgs.add(new CommandLineArgument(commandLineSetup.getToolPath(), CommandLineArgument.Type.PARAMETER));
    cmdLineArgs.addAll(commandLineSetup.getArgs());

    final RunAsCmdSettings runAsCmdSettings = new RunAsCmdSettings(myCommandLineArgumentsService.createCommandLineString(cmdLineArgs));

    final File cmdFile = myFileService.getTempFileName(myCommandFileExtension);
    resources.add(new CommandLineFile(mySettingsPublisher, cmdFile.getAbsoluteFile(), myRunAsCmdGenerator.create(runAsCmdSettings)));

    return Collections.singleton(
      new CommandLineSetup(
        getTool().getAbsolutePath(),
        Arrays.asList(
          new CommandLineArgument(settingsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
          new CommandLineArgument(settings.getPassword(), CommandLineArgument.Type.PARAMETER)),
        resources));
  }

  private File getTool() {
    final File path = new File(myParametersService.getToolPath(Constants.RUN_AS_TOOL_NAME), TOOL_FILE_NAME + myCommandFileExtension);
    myFileService.validatePath(path);
    return path;
  }
}