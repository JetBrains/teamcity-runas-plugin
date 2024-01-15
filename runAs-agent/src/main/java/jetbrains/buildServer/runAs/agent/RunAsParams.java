

package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import org.jetbrains.annotations.NotNull;

public class RunAsParams {
  private final List myCommandLineArguments;

  public RunAsParams(
    @NotNull final List<CommandLineArgument> commandLineArguments) {

    myCommandLineArguments = commandLineArguments;
  }

  public List<CommandLineArgument> getCommandLineArguments() {
    return myCommandLineArguments;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof RunAsParams)) return false;

    final RunAsParams that = (RunAsParams)o;

    return getCommandLineArguments().equals(that.getCommandLineArguments());

  }

  @Override
  public int hashCode() {
    return getCommandLineArguments().hashCode();
  }
}