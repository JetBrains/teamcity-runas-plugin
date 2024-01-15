

package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BuildFeatureParametersService {
  @Nullable
  String tryGetBuildFeatureParameter(@NotNull final String buildFeatureType, @NotNull final String parameterName);
}