package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class RunAsCmdGenerator implements ResourceGenerator<RunAsArgsSettings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @NotNull
  @Override
  public String create(@NotNull final RunAsArgsSettings settings) {
    final StringBuilder sb = new StringBuilder();

    sb.append("@ECHO OFF");

    sb.append(LINE_SEPARATOR);
    sb.append("PUSHD \"");
    sb.append(settings.getWorkingDirectory());
    sb.append("\"");

    sb.append(LINE_SEPARATOR);
    sb.append(settings.getCommandLine());

    sb.append(LINE_SEPARATOR);
    sb.append("SET \"EXIT_CODE=%ERRORLEVEL%\"");

    sb.append(LINE_SEPARATOR);
    sb.append("POPD");

    sb.append(LINE_SEPARATOR);
    sb.append("EXIT /B %EXIT_CODE%");

    return sb.toString();
  }
}
