/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProfileParametersServiceImpl implements ProfileParametersService {
  private static final Logger LOG = Logger.getInstance(ProfileParametersServiceImpl.class.getName());
  private final Map<String, Configuration> myProfiles = new HashMap<String, Configuration>();
  private final AgentParametersService myAgentParametersService;
  private final PathsService myPathsService;
  private final FileService myFileService;
  private final CryptographicService myCryptographicService;

  public ProfileParametersServiceImpl(
    @NotNull final AgentParametersService runnerParametersService,
    @NotNull final PathsService pathsService,
    @NotNull final FileService fileService,
    @NotNull final CryptographicService cryptographicService) {
    myAgentParametersService = runnerParametersService;
    myPathsService = pathsService;
    myFileService = fileService;
    myCryptographicService = cryptographicService;
  }

  @Override
  public void load() {
    myProfiles.clear();
    final String credentialsDirectoryStr = myAgentParametersService.tryGetConfigParameter(jetbrains.buildServer.runAs.common.Constants.CREDENTIALS_DIRECTORY);
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
  public Set<String> getProfiles() {
    return myProfiles.keySet();
  }

  @Nullable
  public String tryGetProperty(@NotNull final String profile, @NotNull final String key) {
    Configuration properties = myProfiles.get(profile);
    if(properties == null) {
      properties = myProfiles.get(profile + ".properties");
      if(properties == null) {
        return null;
      }
    }

    return properties.getProperty(key);
  }

  private Configuration CreateConfiguration()
  {
    return new ConfigurationImpl();
  }

  private void load(@NotNull final File propertyFile) {
    Configuration properties = CreateConfiguration();
    try {
      if(LOG.isDebugEnabled()) {
        LOG.debug("Loading credentials from \"" + propertyFile + "\"");
      }
      final ByteArrayInputStream stream = new ByteArrayInputStream(myFileService.readAllTextFile(propertyFile).getBytes(StandardCharsets.UTF_8));
      properties.load(stream);

      for(Map.Entry<Object, Object> entry: properties.entrySet())
      {
        if(!(entry.getValue() instanceof String))
        {
          continue;
        }

        final String decryptedString = myCryptographicService.unscramble((String)entry.getValue());
        entry.setValue(decryptedString);
      }


      if(LOG.isDebugEnabled()) {
        LOG.debug(properties.size() + " properties were loaded from \"" + propertyFile + "\"");
      }
    }
    catch (IOException ex){
      LOG.error("Error occurred during loading property file \"" + propertyFile + "\"", ex);
    }

    myProfiles.put(propertyFile.getName(), properties);
  }
}
