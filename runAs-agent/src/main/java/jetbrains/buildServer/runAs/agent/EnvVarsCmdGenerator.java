package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

public class EnvVarsCmdGenerator implements ResourceGenerator<List<EnvironmentVariable>> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @NotNull
  @Override
  public String create(@NotNull final List<EnvironmentVariable> environmentVariables) {
    final List<String> lines = new ArrayList<String>();
    lines.add("@ECHO OFF");
    lines.addAll(
      CollectionsUtil.convertCollection(
        environmentVariables,
        new Converter<String, EnvironmentVariable>() {
          @Override
          public String createFrom(@NotNull final EnvironmentVariable environmentVariable) {
            return "ECHO SET \"" + environmentVariable.getName() + "=%" + environmentVariable.getName() + "%\"";
          }
        }));

    return StringUtil.join(lines, LINE_SEPARATOR);
  }
}
