package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PropertiesService {
  void load();

  @NotNull
  Set<String> getPropertySets();

  @Nullable
  String tryGetProperty(@NotNull final String propertySet, @NotNull final String key);
}
