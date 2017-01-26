package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.util.ArrayList;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.LoggerService;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.runAs.agent.Constants.PASSWORD_REPLACEMENT_VAL;
import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_LOG_ENABLED;

public class RunAsLoggerImpl implements RunAsLogger {
  private static final Logger LOG = Logger.getInstance(RunAsLoggerImpl.class.getName());
  private final LoggerService myLoggerService;
  private final PathsService myPathsService;
  private final SecuredLoggingService mySecuredLoggingService;
  private final RunnerParametersService myRunnerParametersService;

  public RunAsLoggerImpl(
    @NotNull final LoggerService loggerService,
    @NotNull final PathsService pathsService,
    @NotNull final SecuredLoggingService securedLoggingService,
    @NotNull final RunnerParametersService runnerParametersService) {
    myLoggerService = loggerService;
    myPathsService = pathsService;
    mySecuredLoggingService = securedLoggingService;
    myRunnerParametersService = runnerParametersService;
  }

  @Override
  public void LogRunAs(
    @NotNull final UserCredentials userCredentials,
    @NotNull final CommandLineSetup baseCommandLineSetup,
    @NotNull final CommandLineSetup runAsCommandLineSetup) {
    mySecuredLoggingService.disableLoggingOfCommandLine();
    final GeneralCommandLine baseCmd = convertToCommandLine(baseCommandLineSetup, false);
    final GeneralCommandLine runAsCmd = convertToCommandLine(runAsCommandLineSetup, true);

    LOG.info("Run as user \"" + userCredentials.getUser() + "\": " + runAsCmd.getCommandLineString());
    if(Boolean.parseBoolean(myRunnerParametersService.tryGetConfigParameter(RUN_AS_LOG_ENABLED))) {
      myLoggerService.onStandardOutput("Starting: " + runAsCmd.getCommandLineString());
      myLoggerService.onStandardOutput("as user: " + userCredentials.getUser());
    }

    myLoggerService.onStandardOutput("Starting: " + baseCmd.getCommandLineString());

    File workingDirectory = runAsCommandLineSetup.getWorkingDirectory();
    if(workingDirectory == null) {
      workingDirectory = myPathsService.getPath(WellKnownPaths.Checkout);
    }

    myLoggerService.onStandardOutput("in directory: " + workingDirectory);
  }

  private GeneralCommandLine convertToCommandLine(@NotNull final CommandLineSetup runAsCommandLineSetup, boolean replacePassword)
  {
    final GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(runAsCommandLineSetup.getToolPath());
    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>(runAsCommandLineSetup.getArgs());
    if(replacePassword && args.size() > 0) {
      args.remove(args.size() - 1);
      args.add(new CommandLineArgument(PASSWORD_REPLACEMENT_VAL, CommandLineArgument.Type.PARAMETER));
    }

    for(final CommandLineArgument arg: args)
    {
      cmd.addParameter(arg.getValue());
    }

    return cmd;
  }
}
