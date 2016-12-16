package jetbrains.buildServer.runAs.agent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesServiceImpl implements PropertiesService {
  private final Properties myProperty = new Properties();
  private final FileService myFileService;

  public PropertiesServiceImpl(@NotNull final FileService fileService) {
    myFileService = fileService;
  }

  public void load(@NotNull final File propertyFile) {
    myProperty.clear();
    try {
      final ByteArrayInputStream stream = new ByteArrayInputStream(myFileService.readAllTextFile(propertyFile).getBytes(StandardCharsets.UTF_8));
      myProperty.load(stream);
    } catch (IOException e) {
      throw new BuildStartException("Error occurred while reading the file \"" + propertyFile.getName() + "\"", e);
    }
  }

  @Nullable
  public String tryGetProperty(String key) {
    return myProperty.getProperty(key);
  }
}
