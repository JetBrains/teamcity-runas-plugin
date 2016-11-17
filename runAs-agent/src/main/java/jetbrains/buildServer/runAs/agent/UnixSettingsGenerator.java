package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

public class UnixSettingsGenerator implements ResourceGenerator<Settings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "--user";

  @NotNull
  @Override
  public String create(@NotNull final Settings settings) {
    final StringBuilder sb = new StringBuilder();

    final String user = settings.getUser();
    if(!StringUtil.isEmptyOrSpaces(user)) {
      sb.append(USER_CMD_KEY);
      sb.append(' ');
      sb.append(settings.getUser());
    }
    
    return sb.toString();
  }
}