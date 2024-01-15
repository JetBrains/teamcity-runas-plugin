

package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AgentParametersService {
  @Nullable
  String tryGetConfigParameter(@NotNull String parameterName);
}