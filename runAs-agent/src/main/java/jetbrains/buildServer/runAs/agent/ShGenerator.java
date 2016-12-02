package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class ShGenerator implements ResourceGenerator<Params> {
  public static final String BASH_HEADER = "#!/bin/bash";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String NORMAL_STATUS = "NORMAL";

  @NotNull
  @Override
  public String create(@NotNull final Params settings) {
    final String cmdLine = settings.getCommandLine();
    final StringBuilder sb = new StringBuilder();
    sb.append(BASH_HEADER);
    sb.append(LINE_SEPARATOR);
    sb.append("echo \"");
    sb.append(new Message("Starting: " + settings.getCommandLine(), NORMAL_STATUS, null).toString().replace("\"", "\\\""));
    sb.append("\"");
    sb.append(LINE_SEPARATOR);
    sb.append(cmdLine);
    return sb.toString();
  }
}
