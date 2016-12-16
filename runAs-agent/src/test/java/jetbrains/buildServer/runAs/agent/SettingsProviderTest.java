package jetbrains.buildServer.runAs.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class SettingsProviderTest {
  private Mockery myCtx;
  private ParametersService myParametersService;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private UserCredentialsService myUserCredentialsService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myParametersService = myCtx.mock(ParametersService.class);
    myUserCredentialsService = myCtx.mock(UserCredentialsService.class);
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
  }

  @DataProvider(name = "provideSettingsCases")
  public Object[][] getProvideSettingsCases() {
    return new Object[][] {
      {
        new UserCredentials("user1", "password1"),
        new HashMap<String, String>() {{ put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new Settings(new UserCredentials("user1", "password1"), WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },

      {
        null,
        new HashMap<String, String>() {{ put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        null
      },

      {
        new UserCredentials("user1", "password1"),
        new HashMap<String, String>() {{ put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); put(Constants.WINDOWS_INTEGRITY_LEVEL_VAR, WindowsIntegrityLevel.High.getValue()); }},
        new Settings(new UserCredentials("user1", "password1"), WindowsIntegrityLevel.High, LoggingLevel.Off, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new UserCredentials("user1", "password1"),
        new HashMap<String, String>() {{ put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); put(Constants.WINDOWS_INTEGRITY_LEVEL_VAR, WindowsIntegrityLevel.High.getValue()); put(Constants.WINDOWS_LOGGING_LEVEL_VAR, LoggingLevel.Debug.getValue()); }},
        new Settings(new UserCredentials("user1", "password1"), WindowsIntegrityLevel.High, LoggingLevel.Debug, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },
    };
  }

  @Test(dataProvider = "provideSettingsCases")
  public void shouldProvideSettings(
    @Nullable final UserCredentials userCredentials,
    @NotNull final HashMap<String, String> parameters,
    @Nullable final Settings expectedSettings) throws IOException {
    // Given
    myCtx.checking(new Expectations() {{
      oneOf(myUserCredentialsService).tryGetUserCredentials();
      will(returnValue(userCredentials));

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

      allowing(myParametersService).tryGetParameter(with(any(String.class)));
      will(new CustomAction("tryGetParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return parameters.get(name);
        }
      });
    }});

    final SettingsProvider provider = createInstance();

    // When
    final Settings actualSettings = provider.tryGetSettings();

    // Then
    myCtx.assertIsSatisfied();
    then(actualSettings).isEqualTo(expectedSettings);
  }

  @NotNull
  private SettingsProvider createInstance()
  {
    return new SettingsProviderImpl(
      myParametersService,
      myUserCredentialsService,
      myCommandLineArgumentsService);
  }
}
