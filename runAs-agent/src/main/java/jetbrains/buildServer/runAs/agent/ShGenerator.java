

package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class ShGenerator implements ResourceGenerator<RunAsParams> {
  public static final String BASH_HEADER = "#!/bin/bash";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private final Converter<String, String> myArgumentConverter;

  public ShGenerator(
    @NotNull final Converter<String, String> argumentConverter) {
    myArgumentConverter = argumentConverter;
  }

  @NotNull
  @Override
  public String create(@NotNull final RunAsParams settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(BASH_HEADER);
    sb.append(LINE_SEPARATOR);

    boolean first = true;
    for (CommandLineArgument arg: settings.getCommandLineArguments()) {
      if(first) {
        first = false;
      }
      else {
        sb.append(' ');
      }

      sb.append(myArgumentConverter.convert(arg.getValue()));
    }

    return sb.toString();
  }
}