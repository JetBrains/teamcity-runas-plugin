package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.File;
import java.util.ArrayList;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.LoggerService;
import org.jetbrains.annotations.NotNull;

public class RunAsLoggerImpl implements RunAsLogger {
  private static final String PASSWORD_REPLACEMENT_VAL = "*****";
  private final LoggerService myLoggerService;
  private final FileService myFileService;
  private final ParametersService myParametersService;

  public RunAsLoggerImpl(
    @NotNull final LoggerService loggerService,
    @NotNull final FileService fileService,
    @NotNull final ParametersService parametersService) {
    myLoggerService = loggerService;
    myFileService = fileService;
    myParametersService = parametersService;
  }

  @Override
  public void LogRunAs(@NotNull final CommandLineSetup runAsCommandLineSetup) {
    myParametersService.disableLoggingOfCommandLine();

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
      workingDirectory = myFileService.getCheckoutDirectory();
    }

    myLoggerService.onStandardOutput("in directory: " + workingDirectory);
  }
}
