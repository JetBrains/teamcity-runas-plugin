package jetbrains.buildServer.runAs.agent;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BuildFeatureParametersService {
  @NotNull
  List<String> getBuildFeatureParameters(@NotNull final String buildFeatureType, @NotNull final String parameterName);

  void setBuildFeatureParameters(@NotNull final String buildFeatureType, @NotNull final String parameterName, @Nullable final String parameterValue);
}