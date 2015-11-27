package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandLineExecutorImpl implements CommandLineExecutor {
  @Nullable
  public ExecResult runProcess(@NotNull final CommandLineSetup commandLineSetup, final int executionTimeoutSeconds) throws ExecutionException {
    try {
      final GeneralCommandLine cmd = new GeneralCommandLine();
      cmd.setExePath(commandLineSetup.getToolPath());
      for (CommandLineArgument arg: commandLineSetup.getArgs()) {
        cmd.addParameter(arg.getValue());
      }

      final jetbrains.buildServer.CommandLineExecutor executor = new jetbrains.buildServer.CommandLineExecutor(cmd);
      return executor.runProcess(executionTimeoutSeconds);
    }
    catch (RuntimeException ex) {
      throw  new ExecutionException(ex.getMessage());
    }
  }
}
