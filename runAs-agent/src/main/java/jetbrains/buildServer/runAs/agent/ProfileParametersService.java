

package jetbrains.buildServer.runAs.agent;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProfileParametersService {
  void load();

  @NotNull
  Set<String> getProfiles();

  @Nullable
  String tryGetProperty(@NotNull final String profile, @NotNull final String key);
}