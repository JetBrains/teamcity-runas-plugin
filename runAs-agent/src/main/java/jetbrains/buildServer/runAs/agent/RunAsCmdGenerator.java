package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class RunAsCmdGenerator implements ResourceGenerator<RunAsCmdSettings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String NORMAL_STATUS = "NORMAL";

  @NotNull
  @Override
  public String create(@NotNull final RunAsCmdSettings settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append("@ECHO OFF");

    sb.append(LINE_SEPARATOR);
    sb.append("PUSHD \"");
    sb.append(settings.getWorkingDirectory());
    sb.append("\"");

    sb.append(LINE_SEPARATOR);
    sb.append("ECHO ");
    sb.append(new Message("Starting: " + settings.getCommandLine(), NORMAL_STATUS, null).toString());

    sb.append(LINE_SEPARATOR);
    sb.append("ECHO ");
    sb.append(new Message("in directory: " + settings.getWorkingDirectory(), NORMAL_STATUS, null).toString());

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
