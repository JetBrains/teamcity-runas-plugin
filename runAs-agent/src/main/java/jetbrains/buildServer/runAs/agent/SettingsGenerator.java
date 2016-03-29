package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

public class SettingsGenerator implements ResourceGenerator<Settings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "-u:";

  @NotNull
  @Override
  public String create(@NotNull final Settings credentials) {
    final StringBuilder sb = new StringBuilder();

    sb.append(USER_CMD_KEY);
    sb.append(credentials.getUser());
    if(credentials.getAdditionalArgs().size() > 0) {
      sb.append(LINE_SEPARATOR);
    }

    sb.append(
      StringUtil.join(
        CollectionsUtil.convertCollection(
          credentials.getAdditionalArgs(),
          new Converter<String, CommandLineArgument>() {
            @Override
            public String createFrom(@NotNull final CommandLineArgument commandLineArgument) {
              return commandLineArgument.getValue();
            }
        }),
        LINE_SEPARATOR));

    return sb.toString();
  }
}