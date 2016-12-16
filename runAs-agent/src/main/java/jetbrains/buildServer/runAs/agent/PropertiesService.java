package jetbrains.buildServer.runAs.agent;

import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PropertiesService {
  void load(@NotNull final File propertyFile);

  @Nullable
  String tryGetProperty(String key);
}
