package jetbrains.buildServer.runAs.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesToConfigurationAdapter implements Configuration {
  private final Properties myProperties = new Properties();

  @Override
  public void load(@NotNull final ByteArrayInputStream stream) throws IOException {
    myProperties.load(stream);
  }

  @Override
  public int size() {
    return myProperties.size();
  }

  @NotNull
  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {
    return myProperties.entrySet();
  }

  @Nullable
  @Override
  public String getProperty(@NotNull final String key) {
    return myProperties.getProperty(key);
  }
}
