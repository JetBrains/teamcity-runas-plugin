

package jetbrains.buildServer.runAs.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Configuration {
  void load(@NotNull ByteArrayInputStream stream) throws IOException;

  int size();

  @NotNull
  Set<Map.Entry<Object, Object>> entrySet();

  @Nullable String getProperty(@NotNull String key);
}