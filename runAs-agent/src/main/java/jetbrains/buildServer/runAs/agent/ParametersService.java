package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParametersService {
  @Nullable
  String tryGetParameter(@NotNull final String paramName);
}
