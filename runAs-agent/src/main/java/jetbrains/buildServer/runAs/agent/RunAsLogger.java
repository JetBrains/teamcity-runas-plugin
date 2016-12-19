package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;

public interface RunAsLogger {
  void LogRunAs(@NotNull final CommandLineSetup runAsCommandLineSetup);
}
