package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class SettingsGenerator implements ResourceGenerator<Settings> {
  private static final String ourLineSeparator = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "-u:";
  private static final String PASSWORD_CMD_KEY = "-p:";
  private static final String WORKING_DIRECTORY_CMD_KEY = "-w:";

  @NotNull
  @Override
  public String create(@NotNull final Settings settings) {
    final StringBuilder sb = new StringBuilder();

    sb.append(USER_CMD_KEY);
    sb.append(settings.getUser());
    sb.append(ourLineSeparator);

    sb.append(PASSWORD_CMD_KEY);
    sb.append(settings.getPassword());
    sb.append(ourLineSeparator);

    sb.append(WORKING_DIRECTORY_CMD_KEY);
    sb.append(settings.getWorkingDirectory());

    return sb.toString();
  }
}