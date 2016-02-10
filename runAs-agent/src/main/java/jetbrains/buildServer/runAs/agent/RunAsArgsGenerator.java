package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class RunAsArgsGenerator implements ResourceGenerator<RunAsArgsSettings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String QUOTE = "\"";
  private static final String WORKING_DIRECTORY_ARG = "-w:";
  private static final String CMD_EXE_ARG = "cmd.exe";
  private static final String CMD_S_ARG = "/S";
  private static final String CMD_C_ARG = "/C";

  @NotNull
  @Override
  public String create(@NotNull final RunAsArgsSettings settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(WORKING_DIRECTORY_ARG);
    sb.append(settings.getWorkingDirectory());
    sb.append(LINE_SEPARATOR);

    sb.append(CMD_EXE_ARG);
    sb.append(LINE_SEPARATOR);

    sb.append(CMD_S_ARG);
    sb.append(LINE_SEPARATOR);

    sb.append(CMD_C_ARG);
    sb.append(LINE_SEPARATOR);

    sb.append(QUOTE);
    sb.append(settings.getCommandLine());
    sb.append(QUOTE);

    return sb.toString();
  }
}
