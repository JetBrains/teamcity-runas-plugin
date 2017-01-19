package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.File;
import java.util.ArrayList;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.LoggerService;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.runAs.agent.Constants.PASSWORD_REPLACEMENT_VAL;

public class RunAsLoggerImpl implements RunAsLogger {
  private final LoggerService myLoggerService;
  private final PathsService myPathsService;
  private final SecuredLoggingService mySecuredLoggingService;

  public RunAsLoggerImpl(
    @NotNull final LoggerService loggerService,
    @NotNull final PathsService pathsService,
    @NotNull final SecuredLoggingService securedLoggingService) {
    myLoggerService = loggerService;
    myPathsService = pathsService;
    mySecuredLoggingService = securedLoggingService;
  }

  @Override
  public void LogRunAs(@NotNull final CommandLineSetup runAsCommandLineSetup) {
    mySecuredLoggingService.disableLoggingOfCommandLine();

    final GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(runAsCommandLineSetup.getToolPath());
    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>(runAsCommandLineSetup.getArgs());
    if(args.size() > 0) {
      args.remove(args.size() - 1);
      args.add(new CommandLineArgument(PASSWORD_REPLACEMENT_VAL, CommandLineArgument.Type.PARAMETER));
    }

    for(final CommandLineArgument arg: args)
    {
      cmd.addParameter(arg.getValue());
    }

    myLoggerService.onStandardOutput("Starting: " + cmd.getCommandLineString());


    File workingDirectory = runAsCommandLineSetup.getWorkingDirectory();
    if(workingDirectory == null) {
      workingDirectory = myPathsService.getPath(WellKnownPaths.Checkout);
    }

    myLoggerService.onStandardOutput("in directory: " + workingDirectory);
  }
}
