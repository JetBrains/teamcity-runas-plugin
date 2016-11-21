package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class ShGenerator implements ResourceGenerator<Params> {

  @NotNull
  @Override
  public String create(@NotNull final Params settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(settings.getCommandLine());
    return sb.toString();
  }
}
