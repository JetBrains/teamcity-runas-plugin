package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.util.StringUtils;
import org.jetbrains.annotations.NotNull;

public class SettingsGenerator implements ResourceGenerator<Settings> {
  private static final String ourLineSeparator = System.getProperty("line.separator");
  private static final String USER_CMD_KEY = "-u:";
  private static final String PASSWORD_CMD_KEY = "-p:";
  private static final String WORKING_DIRECTORY_CMD_KEY = "-w:";
  private final CommandLineArgumentsService myCommandLineArgumentsService;

  public SettingsGenerator(
    @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    myCommandLineArgumentsService = commandLineArgumentsService;
  }

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
    sb.append(ourLineSeparator);

    sb.append(settings.getSetup().getToolPath());

    for(CommandLineArgument cmdArg: myCommandLineArgumentsService.normalizeCommandLineArguments(settings.getSetup().getArgs())){
      sb.append(ourLineSeparator);
      sb.append(cmdArg.getValue());
    }

    return sb.toString();
  }
}