package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesServiceImpl implements PropertiesService {
  private static final Logger LOG = Logger.getInstance(PropertiesServiceImpl.class.getName());
  private final Map<String, Properties> myPropertySets = new HashMap<String, Properties>();
  private final RunnerParametersService myRunnerParametersService;
  private final PathsService myPathsService;
  private final FileService myFileService;

  public PropertiesServiceImpl(
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final PathsService pathsService,
    @NotNull final FileService fileService) {
    myRunnerParametersService = runnerParametersService;
    myPathsService = pathsService;
    myFileService = fileService;
  }

  @Override
  public void load() {
    myPropertySets.clear();
    final String credentialsDirectoryStr = myRunnerParametersService.tryGetConfigParameter(jetbrains.buildServer.runAs.common.Constants.CREDENTIALS_DIRECTORY);
    if(StringUtil.isEmptyOrSpaces(credentialsDirectoryStr)) {
      LOG.error("Configuration parameter \"" + Constants.CREDENTIALS_DIRECTORY + "\" was not defined");
      return;
    }

    if(LOG.isDebugEnabled()) {
      LOG.debug("Credentials directory value is \"" + credentialsDirectoryStr + "\"");
    }

    File credentialsDirectory = new File(credentialsDirectoryStr);
    if(!myFileService.exists(credentialsDirectory) || !myFileService.isAbsolute(credentialsDirectory))
    {
      credentialsDirectory = new File(myPathsService.getPath(WellKnownPaths.Bin), credentialsDirectoryStr);
    }

    if(LOG.isDebugEnabled()) {
      LOG.debug("Credentials directory is \"" + credentialsDirectory + "\"");
    }

    if(!myFileService.exists(credentialsDirectory) || !myFileService.isDirectory(credentialsDirectory)) {
      LOG.error("Credentials directory \"" + credentialsDirectory + "\" was not found");
      return;
    }

    for(File propertyFiles: myFileService.listFiles(credentialsDirectory)) {
      load(propertyFiles);
    }
  }

  @NotNull
  @Override
  public Set<String> getPropertySets() {
    return myPropertySets.keySet();
  }

  @Nullable
  public String tryGetProperty(@NotNull final String propertySet, @NotNull final String key) {
    Properties properties = myPropertySets.get(propertySet);
    if(properties == null) {
      properties = myPropertySets.get(propertySet + ".properties");
      if(properties == null) {
        return null;
      }
    }

    return properties.getProperty(key);
  }

  private void load(@NotNull final File propertyFile) {
    Properties properties = new Properties();
    try {
      if(LOG.isDebugEnabled()) {
        LOG.debug("Loading credentials from \"" + propertyFile + "\"");
      }
      final ByteArrayInputStream stream = new ByteArrayInputStream(myFileService.readAllTextFile(propertyFile).getBytes(StandardCharsets.UTF_8));
      properties.load(stream);

      if(LOG.isDebugEnabled()) {
        LOG.debug(properties.size() + " properties were loaded from \"" + propertyFile + "\"");
      }
    }
    catch (IOException ex){
      LOG.error("Error occurred during loading property file \"" + propertyFile + "\"", ex);
    }

    myPropertySets.put(propertyFile.getName(), properties);
  }
}
