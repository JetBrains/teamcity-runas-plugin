package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

public class WindowsSettingsGenerator implements ResourceGenerator<Settings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "-u:";
  private static final String WINDOWS_INTEGRITY_LEVEL_CMD_KEY = "-il:";

  @NotNull
  @Override
  public String create(@NotNull final Settings settings) {
    final StringBuilder sb = new StringBuilder();

    final String user = settings.getUser();
    if(!StringUtil.isEmptyOrSpaces(user)) {
      sb.append(USER_CMD_KEY);
      sb.append(settings.getUser());
    }

    if(settings.getWindowsIntegrityLevel() != WindowsIntegrityLevel.Auto) {
      if(sb.length() > 0)
      {
        sb.append(LINE_SEPARATOR);
      }

      sb.append(WINDOWS_INTEGRITY_LEVEL_CMD_KEY);
      sb.append(settings.getWindowsIntegrityLevel().getValue());
    }

    if(sb.length() > 0 && settings.getAdditionalArgs().size() > 0) {
      sb.append(LINE_SEPARATOR);
    }

    sb.append(
      StringUtil.join(
        CollectionsUtil.convertCollection(
          settings.getAdditionalArgs(),
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