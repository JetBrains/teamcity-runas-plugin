package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class CredentialsGenerator implements ResourceGenerator<CredentialsSettings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "-u:";
  private static final String PASSWORD_CMD_KEY = "-p:";

  @NotNull
  @Override
  public String create(@NotNull final CredentialsSettings credentials) {
    final StringBuilder sb = new StringBuilder();

    sb.append(USER_CMD_KEY);
    sb.append(credentials.getUser());
    sb.append(LINE_SEPARATOR);

    sb.append(PASSWORD_CMD_KEY);
    sb.append(credentials.getPassword());
    sb.append(LINE_SEPARATOR);

    return sb.toString();
  }
}