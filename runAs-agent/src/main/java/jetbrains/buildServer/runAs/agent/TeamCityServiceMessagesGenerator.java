package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class TeamCityServiceMessagesGenerator implements ResourceGenerator<RunAsArgsSettings> {
  private static final String NORMAL_STATUS = "NORMAL";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @NotNull
  @Override
  public String create(@NotNull final RunAsArgsSettings settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(new Message("Starting: " + settings.getCommandLine(), NORMAL_STATUS, null).toString());
    sb.append(LINE_SEPARATOR);
    sb.append(new Message("in directory: " + settings.getWorkingDirectory(), NORMAL_STATUS, null).toString());
    return sb.toString();
  }
}
