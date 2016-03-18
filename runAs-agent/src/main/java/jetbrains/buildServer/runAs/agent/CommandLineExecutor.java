package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandLineExecutor {
  @Nullable
  ExecResult runProcess(@NotNull final CommandLineSetup commandLineSetup, final int executionTimeoutSeconds) throws ExecutionException;
}
