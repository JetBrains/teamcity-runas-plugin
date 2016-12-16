package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.BuildAgentConfigurationEx;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.CredentialsMode;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class UserCredentialsServiceTest {
  private Mockery myCtx;
  private RunnerParametersService myRunnerParametersService;
  private ParametersService myParametersService;
  private PropertiesService myPropertiesService;
  private BuildAgentConfigurationEx myBuildAgentConfiguration;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private File myConfigDir;
  private File runAsCredDir;
  private File user2Cred;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myParametersService = myCtx.mock(ParametersService.class);
    myPropertiesService = myCtx.mock(PropertiesService.class);
    myBuildAgentConfiguration = myCtx.mock(BuildAgentConfigurationEx.class);
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);

    myConfigDir = new File("configDir");
    runAsCredDir = new File(myConfigDir, "runAsCredDir");
    user2Cred = new File(runAsCredDir, "user2cred.properties");
  }

  @DataProvider(name = "getUserCredentialsCases")
  public Object[][] getUserCredentialsCases() {
    return new Object[][] {
      // Enforced && predefined credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
          new UserCredentials("user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Enforced && predefined credentials && throw exception WHEN password is empty
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        "Password must be defined"
      },

      // Enforced && predefined credentials && throw exception WHEN password is null
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        "Password must be defined"
      },

      // Enforced && predefined credentials WHEN throw exception when user is empty
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        "Username must be defined"
      },

      // Enforced && predefined credentials WHEN throw exception when user is null
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        "Username must be defined"
      },

      // Enforced && predefined credentials WHEN cred file is directory
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualDirectory(user2Cred)),
        null,
        "Credentials file .* was not found"
      },

      // Enforced && predefined credentials WHEN there is not cred file
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir)),
        null,
        "Credentials file .* was not found"
      },

      // Enforced && predefined credentials WHEN cred dir is not a dir
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualFile(runAsCredDir, "")),
        null,
        "Credentials directory was not found"
      },

      // Enforced && predefined credentials WHEN cred dir does not exist
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(),
        null,
        "Credentials directory was not found"
      },

      // Enforced && predefined credentials WHEN CREDENTIALS_DIRECTORY_VAR is not defined as configuration parameter
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(),
        null,
        "Configuration parameter \"" + Constants.CREDENTIALS_DIRECTORY_VAR + "\" was not defined"
      },

      // Enforced && predefined credentials WHEN CREDENTIALS_VAR is not defined as configuration parameter
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(),
        null,
        "Configuration parameter \"" + Constants.CREDENTIALS_VAR + "\" was not defined"
      },

      // Allowed by default && custom credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1"); }},
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new VirtualFileService(),
        new UserCredentials("user1", "password1", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Allowed && custom credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1"); }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Allowed.getValue()); }},
        new HashMap<String, String>(),
        new VirtualFileService(),
        new UserCredentials("user1", "password1", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Allowed && predefined credentials WHEN custom credentials is not defined
      {
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_VAR, "user2cred");}},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Allowed.getValue());
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        new UserCredentials("user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Allowed && predefined credentials WHEN custom credentials is not defined
      {
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_VAR, "user10000cred");}},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Allowed.getValue());
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        "Credentials file for .* was not found"
      },

      // Allowed && predefined credentials && ret null WHEN credentials is not defined at all
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Allowed.getValue());
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        null
      },

      // Enforced && custom credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1"); }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir"); }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        new UserCredentials("user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Enforced && custom credentials && throw an exception WHEN CREDENTIALS_VAR is not defined as configuration parameter
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1"); }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Enforced.getValue());
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir"); }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        "Configuration parameter \"" + Constants.CREDENTIALS_VAR + "\" was not defined"
      },

      // Disabled && ret null
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Disabled.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        null,
        null
      },

      // Disabled && ret null
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Disabled.getValue());
          put(Constants.CREDENTIALS_VAR, "user2cred");
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>(),
        new VirtualFileService(),
        null,
        null
      },

      // Disabled && ret null
      {
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new VirtualFileService(),
        null,
        null
      },

      // Allowed && custom credentials && additional params
      {
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user1");
          put(Constants.PASSWORD_VAR, "password1");
          put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2");
          put(Constants.WINDOWS_INTEGRITY_LEVEL_VAR, WindowsIntegrityLevel.High.getValue());
          put(Constants.WINDOWS_LOGGING_LEVEL_VAR, LoggingLevel.Debug.getValue());
        }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Allowed.getValue());
        }},
        new HashMap<String, String>(),
        new VirtualFileService(),
        new UserCredentials("user1", "password1", WindowsIntegrityLevel.High, LoggingLevel.Debug, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER))),
        null
      },

      // Allowed && predefined credentials && additional params WHEN custom credentials is not defined
      {
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_VAR, "user2cred");}},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_MODE_VAR, CredentialsMode.Allowed.getValue());
          put(Constants.CREDENTIALS_DIRECTORY_VAR, "runAsCredDir");
        }},
        new HashMap<String, String>() {{
          put(Constants.USER_VAR, "user2");
          put(Constants.PASSWORD_VAR, "password2");
          put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2");
          put(Constants.WINDOWS_INTEGRITY_LEVEL_VAR, WindowsIntegrityLevel.High.getValue());
          put(Constants.WINDOWS_LOGGING_LEVEL_VAR, LoggingLevel.Debug.getValue());
        }},
        new VirtualFileService(
          new VirtualFileService.VirtualDirectory(runAsCredDir),
          new VirtualFileService.VirtualFile(user2Cred, "")),
        new UserCredentials("user2", "password2", WindowsIntegrityLevel.High, LoggingLevel.Debug, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER))),
        null
      },
    };
  }

  @Test(dataProvider = "getUserCredentialsCases")
  public void shouldGetUserCredentials(
    @NotNull final HashMap<String, String> parameters,
    @NotNull final HashMap<String, String> configParameters,
    @NotNull final HashMap<String, String> properties,
    @NotNull VirtualFileService fileService,
    @Nullable final UserCredentials expectedUserCredentials,
    @Nullable final String expectedExceptionMessage) throws IOException {
    // Given
    myCtx.checking(new Expectations() {{
      oneOf(myBuildAgentConfiguration).getAgentConfDirectory();
      will(returnValue(myConfigDir));

      allowing(myParametersService).tryGetParameter(with(any(String.class)));
      will(new CustomAction("tryGetParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return parameters.get(name);
        }
      });

      allowing(myRunnerParametersService).tryGetConfigParameter(with(any(String.class)));
      will(new CustomAction("tryGetConfigParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return configParameters.get(name);
        }
      });

      allowing(myPropertiesService).tryGetProperty(with(any(String.class)));
      will(new CustomAction("tryGetProperty") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return properties.get(name);
        }
      });

      allowing(myPropertiesService).load(with(any(File.class)));

      allowing(myCommandLineArgumentsService).parseCommandLineArguments(with(any(String.class)));
      will(new CustomAction("parseCommandLineArguments") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
          for(String arg: StringUtil.split((String)invocation.getParameter(0))) {
            args.add(new CommandLineArgument(arg, CommandLineArgument.Type.PARAMETER));
          }

          return args;
        }
      });
    }});

    final UserCredentialsService userCredentialsService = createInstance(fileService);
    BuildStartException actualException = null;

    // When
    UserCredentials actualUserCredentials = null;
    try {
      actualUserCredentials = userCredentialsService.tryGetUserCredentials();
    }
    catch (BuildStartException ex) {
      actualException = ex;
    }

    // Then
    myCtx.assertIsSatisfied();
    then(actualException != null).isEqualTo(expectedExceptionMessage != null);
    if(actualException != null && expectedExceptionMessage != null) {
      Pattern pattern = Pattern.compile(expectedExceptionMessage);
      then(pattern.matcher(actualException.getMessage()).find()).isEqualTo(true);
    }

    then(actualUserCredentials).isEqualTo(expectedUserCredentials);
  }

  @NotNull
  private UserCredentialsService createInstance(FileService fileService)
  {
    return new UserCredentialsServiceImpl(
      myRunnerParametersService,
      myParametersService,
      myPropertiesService,
      fileService,
      myBuildAgentConfiguration,
      myCommandLineArgumentsService);
  }
}
