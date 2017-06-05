package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Collections;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserCredentialsServiceImpl implements UserCredentialsService {
  static final String DEFAULT_CREDENTIALS = "default";
  private static final Logger LOG = Logger.getInstance(UserCredentialsServiceImpl.class.getName());
  private static final AccessControlList OurBeforeStepDefaultAcl = new AccessControlList(Collections.<AccessControlEntry>emptyList());
  private final ParametersService myParametersService;
  private final PropertiesService myPropertiesService;
  private final CommandLineArgumentsService myCommandLineArgumentsService;
  private final TextParser<AccessControlList> myFileAccessParser;

  public UserCredentialsServiceImpl(
    @NotNull final ParametersService parametersService,
    @NotNull final PropertiesService propertiesService,
    @NotNull final CommandLineArgumentsService commandLineArgumentsService,
    @NotNull final TextParser<AccessControlList> fileAccessParser) {
    myParametersService = parametersService;
    myPropertiesService = propertiesService;
    myCommandLineArgumentsService = commandLineArgumentsService;
    myFileAccessParser = fileAccessParser;
  }

  @Nullable
  @Override
  public UserCredentials tryGetUserCredentials() {
    final boolean allowCustomCredentials = ParameterUtils.parseBoolean(myParametersService.tryGetConfigParameter(Constants.ALLOW_CUSTOM_CREDENTIALS), true);
    final boolean allowProfileIdFromServer = ParameterUtils.parseBoolean(myParametersService.tryGetConfigParameter(Constants.ALLOW_PROFILE_ID_FROM_SERVER), false);
    if(allowCustomCredentials && allowProfileIdFromServer) {
      String credentialsRef = myParametersService.tryGetParameter(Constants.CREDENTIALS_PROFILE_ID);
      final boolean profileWasDefined = !StringUtil.isEmptyOrSpaces(credentialsRef);
      UserCredentials profileCredentials = null;
      if(!profileWasDefined) {
        profileCredentials = getPredefinedCredentials(DEFAULT_CREDENTIALS, false);
        if(LOG.isDebugEnabled()) {
          LOG.debug("tryGetUserCredentials predefined \"" + Constants.CREDENTIALS_PROFILE_ID + "\": " + profileCredentials);
        }
      }

      UserCredentials customCredentials = tryGetCustomCredentials();
      if(customCredentials != null) {
        if(profileWasDefined || profileCredentials != null) {
          LOG.info("Build step attempted to use custom credentials while credentials profiles are configured on the agent. Build step has not been executed and build problem was raised.");
          throw new BuildStartException("Build step cannot be executed with custom credentials on this agent. Please contact system administrator.");
        }

        if(LOG.isDebugEnabled()) {
          LOG.debug("tryGetUserCredentials custom: " + customCredentials);
        }

        return customCredentials;
      }

      if (!profileWasDefined) {
        return profileCredentials;
      }

      profileCredentials = getPredefinedCredentials(credentialsRef, true);
      if(LOG.isDebugEnabled()) {
        LOG.debug("tryGetUserCredentials predefined \"" + credentialsRef + "\": " + profileCredentials);
      }

      return profileCredentials;
    }

    if(allowCustomCredentials) {
      UserCredentials customCredentials = tryGetCustomCredentials();
      if(LOG.isDebugEnabled()) {
        LOG.debug("tryGetUserCredentials custom: " + customCredentials);
      }

      return customCredentials;
    }

    if(allowProfileIdFromServer) {
      String credentialsRef = myParametersService.tryGetConfigParameter(Constants.CREDENTIALS_PROFILE_ID);
      if (StringUtil.isEmptyOrSpaces(credentialsRef)) {
        credentialsRef = DEFAULT_CREDENTIALS;
      }

      UserCredentials profileCredentials = getPredefinedCredentials(credentialsRef, true);
      if(LOG.isDebugEnabled()) {
        LOG.debug("tryGetUserCredentials predefined \"" + credentialsRef + "\": " + profileCredentials);
      }

      return profileCredentials;
    }

    LOG.debug("tryGetUserCredentials returns null");
    return null;
  }

  @Nullable
  private UserCredentials tryGetCustomCredentials() {
    final String userName = tryGetFirstNotEmpty(myParametersService.tryGetParameter(Constants.USER));
    final String password = tryGetFirstNotEmpty(myParametersService.tryGetParameter(Constants.PASSWORD), myParametersService.tryGetParameter(Constants.CONFIG_PASSWORD));

    if(StringUtil.isEmptyOrSpaces(userName) || StringUtil.isEmptyOrSpaces(password)) {
      return null;
    }

    return createCredentials(null, userName, password, false, false);
  }

  @Nullable
  private UserCredentials getPredefinedCredentials(@NotNull final String credentials, final boolean trowException) {
    final String userName;
    final String password;

    userName = tryGetFirstNotEmpty(myPropertiesService.tryGetProperty(credentials, Constants.USER));
    if(StringUtil.isEmptyOrSpaces(userName)) {
      if(trowException) {
        throw new BuildStartException("RunAs user must be defined for \"" + credentials + "\"");
      }
      else {
        return null;
      }
    }

    password = tryGetFirstNotEmpty(myPropertiesService.tryGetProperty(credentials, Constants.PASSWORD), myPropertiesService.tryGetProperty(credentials, Constants.CONFIG_PASSWORD));
    if(StringUtil.isEmptyOrSpaces(password)) {
      if(trowException) {
        throw new BuildStartException("RunAs password must be defined for \"" + credentials + "\"");
      }
      else {
        return null;
      }
    }

    return createCredentials(credentials, userName, password, true, trowException);
  }

  @NotNull
  private UserCredentials createCredentials(@Nullable final String credentials, @NotNull final String userName, @NotNull final String password, boolean isPredefined, final boolean trowException)
  {
    // Get parameters
    final WindowsIntegrityLevel windowsIntegrityLevel = WindowsIntegrityLevel.tryParse(getParam(credentials, Constants.WINDOWS_INTEGRITY_LEVEL, isPredefined));
    final LoggingLevel loggingLevel = LoggingLevel.tryParse(getParam(credentials, Constants.LOGGING_LEVEL, isPredefined));

    String additionalArgs = tryGetFirstNotEmpty(getParam(credentials, Constants.ADDITIONAL_ARGS, isPredefined));
    if(StringUtil.isEmptyOrSpaces(additionalArgs)) {
      additionalArgs = "";
    }

    AccessControlList beforeStepAcl = OurBeforeStepDefaultAcl;
    if(isPredefined && credentials != null) {
      final String beforeStepAclStr = myPropertiesService.tryGetProperty(credentials, Constants.RUN_AS_BEFORE_STEP_ACL);
      if (!StringUtil.isEmptyOrSpaces(beforeStepAclStr)) {
        final ArrayList<AccessControlEntry> aceList = new ArrayList<AccessControlEntry>();
        for (AccessControlEntry ace : myFileAccessParser.parse(beforeStepAclStr)) {
          if (ace.getAccount().getTargetType() == AccessControlAccountType.User) {
            ace = new AccessControlEntry(ace.getFile(), AccessControlAccount.forUser(userName), ace.getPermissions());
          }
          aceList.add(ace);
        }

        beforeStepAcl = new AccessControlList(aceList);
      }
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
  private String getParam(@Nullable final String credentials, @NotNull final String paramName, boolean isPredefined) {
    if(isPredefined && credentials != null) {
      return myPropertiesService.tryGetProperty(credentials, paramName);
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
