package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandLineExecutorImpl implements CommandLineExecutor {
  private static final Logger LOG = Logger.getInstance(CommandLineExecutorImpl.class.getName());
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @Nullable
  public ExecResult runProcess(@NotNull final CommandLineSetup commandLineSetup, final int executionTimeoutSeconds) throws ExecutionException {
    final GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(commandLineSetup.getToolPath());
    for (CommandLineArgument arg: commandLineSetup.getArgs()) {
      cmd.addParameter(arg.getValue());
    }

    try {
      LOG.info("Exec: " + cmd.getCommandLineString());
      final jetbrains.buildServer.CommandLineExecutor executor = new jetbrains.buildServer.CommandLineExecutor(cmd);
      final ExecResult result = executor.runProcess(executionTimeoutSeconds);
      if(LOG.isDebugEnabled()) {
        StringBuilder resultStr = new StringBuilder();
        if (result != null) {
          resultStr.append("exit code: ");
          resultStr.append(result.getExitCode());
          final String[] outLines = result.getOutLines();
          if (outLines != null) {
            resultStr.append("out: ");
            for (String line : outLines) {
              if (!StringUtil.isEmpty(line)) {
                resultStr.append(line);
              }

              resultStr.append(LINE_SEPARATOR);
            }
          }
        } else {
          resultStr.append("has no result");
        }

        LOG.debug("Result: " + resultStr);
      }

      return result;
    }
    catch (RuntimeException ex) {
      throw new ExecutionException(ex.getMessage());
    }
  }
}
