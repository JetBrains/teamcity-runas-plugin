package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.Collections;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserCredentialsServiceImpl implements UserCredentialsService {
  static final String DEFAULT_CREDENTIALS = "default";
  private static final AccessControlList OurBeforeStepDefaultAcl = new AccessControlList(Collections.<AccessControlEntry>emptyList());
  private final ParametersService myParametersService;
  private final PropertiesService myPropertiesService;
  private final FileService myFileService;
  private final PathsService myPathsService;
  private final CommandLineArgumentsService myCommandLineArgumentsService;
  private final TextParser<AccessControlList> myFileAccessParser;

  public UserCredentialsServiceImpl(
    @NotNull final ParametersService parametersService,
    @NotNull final PropertiesService propertiesService,
    @NotNull final FileService fileService,
    @NotNull final PathsService pathsService,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService,
    @NotNull final TextParser<AccessControlList> fileAccessParser) {
    myParametersService = parametersService;
    myPropertiesService = propertiesService;
    myFileService = fileService;
    myPathsService = pathsService;
    myCommandLineArgumentsService = commandLineArgumentsService;
    myFileAccessParser = fileAccessParser;
  }

  @Nullable
  @Override
  public UserCredentials tryGetUserCredentials() {
    UserCredentials userCredentials;
    final boolean allowCustomCredentials = parseBoolean(myParametersService.tryGetConfigParameter(Constants.ALLOW_CUSTOM_CREDENTIALS), true);
    final boolean allowProfileIdFromServer = parseBoolean(myParametersService.tryGetConfigParameter(Constants.ALLOW_PROFILE_ID_FROM_SERVER), true);
    if(allowCustomCredentials && allowProfileIdFromServer) {
      userCredentials = tryGetCustomCredentials();
      if(userCredentials != null) {
        return userCredentials;
      }

      String credentialsRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_PROFILE_ID);
      if (StringUtil.isEmptyOrSpaces(credentialsRef)) {
        return getPredefinedCredentials("default", false);
      }

      return getPredefinedCredentials(credentialsRef, true);
    }

    if(allowCustomCredentials) {
      String credentialsRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_PROFILE_ID);
      if (!StringUtil.isEmptyOrSpaces(credentialsRef)) {
        throw new BuildStartException("The usage of credentials is prohibited");
      }

      return tryGetCustomCredentials();
    }

    if(allowProfileIdFromServer) {
      String credentialsRef = myParametersService.tryGetConfigParameter(Constants.CREDENTIALS_PROFILE_ID);
      if (StringUtil.isEmptyOrSpaces(credentialsRef)) {
        credentialsRef = DEFAULT_CREDENTIALS;
      }

      return getPredefinedCredentials(credentialsRef, true);
    }

    return null;
  }

  private boolean parseBoolean(@Nullable final String boolStr, final boolean defaultValue) {
    if(StringUtil.isEmptyOrSpaces(boolStr)) {
      return defaultValue;
    }

    if(Boolean.toString(true).equalsIgnoreCase(boolStr)) {
      return true;
    }

    if(Boolean.toString(false).equalsIgnoreCase(boolStr)) {
      return false;
    }

    return defaultValue;
  }

  @Nullable
  private UserCredentials tryGetCustomCredentials() {
    final String userName = tryGetFirstNotEmpty(myParametersService.tryGetParameter(Constants.USER), myParametersService.tryGetParameter(Constants.USER));
    final String password = tryGetFirstNotEmpty(myParametersService.tryGetParameter(Constants.PASSWORD), myParametersService.tryGetParameter(Constants.PASSWORD));

    if(StringUtil.isEmptyOrSpaces(userName) || StringUtil.isEmptyOrSpaces(password)) {
      return null;
    }

    return createCredentials(userName, password, false);
  }

  @Nullable
  private UserCredentials getPredefinedCredentials(@NotNull final String credentials, final boolean trowException) {
    final String userName;
    final String password;

    final String credentialsDirectoryStr = myParametersService.tryGetConfigParameter(Constants.CREDENTIALS_DIRECTORY);
    if(credentialsDirectoryStr == null) {
      if(trowException) {
        throw new BuildStartException("Configuration parameter \"" + Constants.CREDENTIALS_DIRECTORY + "\" was not defined");
      }

      return null;
    }

    final File credentialsDirectory = new File(myPathsService.getPath(WellKnownPaths.Bin), credentialsDirectoryStr);
    if(!myFileService.exists(credentialsDirectory) || !myFileService.isDirectory(credentialsDirectory)) {
      if(trowException) {
        throw new BuildStartException("Credentials directory was not found");
      }

      return null;
    }

    final File credentialsFile = new File(credentialsDirectory, credentials + ".properties");
    if(!myFileService.exists(credentialsFile) || myFileService.isDirectory(credentialsFile)) {
      if(trowException) {
        throw new BuildStartException("Credentials file for \"" + credentials + "\" was not found");
      }

      return null;
    }

    myPropertiesService.load(credentialsFile);
    userName = tryGetFirstNotEmpty(myPropertiesService.tryGetProperty(Constants.USER), myPropertiesService.tryGetProperty(Constants.USER));
    if(StringUtil.isEmptyOrSpaces(userName)) {
      throw new BuildStartException("Username must be defined for \"" + credentials + "\"");
    }

    password = tryGetFirstNotEmpty(myPropertiesService.tryGetProperty(Constants.PASSWORD), myPropertiesService.tryGetProperty(Constants.PASSWORD));
    if(StringUtil.isEmptyOrSpaces(password)) {
      throw new BuildStartException("Password must be defined for \"" + credentials + "\"");
    }

    return createCredentials(userName, password, true);
  }

  @NotNull
  private UserCredentials createCredentials(@NotNull final String userName, @NotNull final String password, boolean isPredefined)
  {
    // Get parameters
    final WindowsIntegrityLevel windowsIntegrityLevel = WindowsIntegrityLevel.tryParse(getParam(Constants.WINDOWS_INTEGRITY_LEVEL, isPredefined));
    final LoggingLevel loggingLevel = LoggingLevel.tryParse(getParam(Constants.LOGGING_LEVEL, isPredefined));

    String additionalArgs = tryGetFirstNotEmpty(getParam(Constants.ADDITIONAL_ARGS, isPredefined), getParam(Constants.ADDITIONAL_ARGS, isPredefined));
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    final String beforeStepAclStr = getParam(Constants.RUN_AS_BEFORE_STEP_ACL, isPredefined);
    AccessControlList beforeStepAcl = OurBeforeStepDefaultAcl;
    if(!StringUtil.isEmptyOrSpaces(beforeStepAclStr)) {
      beforeStepAcl = myFileAccessParser.parse(beforeStepAclStr);
    }

    return new UserCredentials(
      userName,
      password,
      windowsIntegrityLevel,
      loggingLevel,
      myCommandLineArgumentsService.parseCommandLineArguments(additionalArgs),
      beforeStepAcl);
  }

  @Nullable
  private String getParam(@NotNull final String paramName, boolean isPredefined) {
    if(isPredefined) {
      return myPropertiesService.tryGetProperty(paramName);
    }

    return myParametersService.tryGetParameter(paramName);
  }

  @Nullable
  private String tryGetFirstNotEmpty(String ... values) {
    for(String value: values) {
      if(!StringUtil.isEmptyOrSpaces(value)) {
        return value;
      }
    }

    return null;
  }
}
