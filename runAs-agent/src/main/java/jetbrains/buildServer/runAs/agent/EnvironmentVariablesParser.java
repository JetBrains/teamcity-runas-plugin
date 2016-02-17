package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

public class EnvironmentVariablesParser implements TextParser<List<EnvironmentVariable>> {
  private static final String DELIMITERS = System.getProperty("line.separator") + ",";

  @NotNull
  @Override
  public List<EnvironmentVariable> parse(@NotNull final String text) {
    return CollectionsUtil.convertCollection(
      Arrays.asList(StringUtils.tokenizeToStringArray(text, DELIMITERS, false, true)),
      new Converter<EnvironmentVariable, String>() {
        @Override
        public EnvironmentVariable createFrom(@NotNull final String envVarName) {
          return new EnvironmentVariable(envVarName);
        }
      });
  }
}
