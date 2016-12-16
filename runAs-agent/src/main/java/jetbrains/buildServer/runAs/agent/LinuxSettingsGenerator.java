package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

public class LinuxSettingsGenerator implements ResourceGenerator<UserCredentials> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @NotNull
  @Override
  public String create(@NotNull final UserCredentials userCredentials) {
    final StringBuilder sb = new StringBuilder();

    final String user = userCredentials.getUser();
    if(!StringUtil.isEmptyOrSpaces(user)) {
      sb.append(userCredentials.getUser());
    }
    
    return sb.toString();
  }
}