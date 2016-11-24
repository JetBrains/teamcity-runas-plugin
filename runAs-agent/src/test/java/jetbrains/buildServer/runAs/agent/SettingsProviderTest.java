package jetbrains.buildServer.runAs.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
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
  private RunnerParametersService myRunnerParametersService;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private BuildFeatureParametersService myBuildFeatureParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myBuildFeatureParametersService = myCtx.mock(BuildFeatureParametersService.class);
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
  }

  @DataProvider(name = "provideSettingsCases")
  public Object[][] getProvideSettingsCases() {
    return new Object[][] {
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.PASSWORD_VAR, "password1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>(),
        new Settings("user1", "password1", Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.PASSWORD_VAR, "password1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); put(Constants.ADDITIONAL_ARGS_VAR, "arg3, arg4"); }},
        new Settings("user1", "password1", Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.PASSWORD_VAR, "password1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); put(Constants.ADDITIONAL_ARGS_VAR, "arg3, arg4"); }},
        new Settings("user2", "password1", Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); put(Constants.ADDITIONAL_ARGS_VAR, "arg3, arg4"); }},
        new Settings("user1", "password2", Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.PASSWORD_VAR, "password1"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); put(Constants.ADDITIONAL_ARGS_VAR, "arg3, arg4"); }},
        new Settings("user1", "password1", Arrays.asList(new CommandLineArgument("arg3", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg4", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); put(Constants.ADDITIONAL_ARGS_VAR, "arg3, arg4"); }},
        new Settings("user2", "password2", Arrays.asList(new CommandLineArgument("arg3", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg4", CommandLineArgument.Type.PARAMETER)))
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.PASSWORD_VAR, "password1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>(),
        null
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>(),
        null
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>(),
        null
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        null
      },

      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.PASSWORD_VAR, "password1"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); }},
        new Settings("user1", "password1", new ArrayList<CommandLineArgument>())
      },

      {
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user3"); put(Constants.PASSWORD_VAR, "password3"); put(Constants.ADDITIONAL_ARGS_VAR, "arg5 arg6"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user1"); put(Constants.PASSWORD_VAR, "password1"); put(Constants.ADDITIONAL_ARGS_VAR, "arg1 arg2"); }},
        new HashMap<String, String>() {{ put(Constants.USER_VAR, "user2"); put(Constants.PASSWORD_VAR, "password2"); put(Constants.ADDITIONAL_ARGS_VAR, "arg3, arg4"); }},
        new Settings("user3", "password3", Arrays.asList(new CommandLineArgument("arg5", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg6", CommandLineArgument.Type.PARAMETER)))
      },
    };
  }

  @Test(dataProvider = "provideSettingsCases")
  public void shouldProvideSettings(
    @NotNull final HashMap<String, String> runnerParameters,
    @NotNull final HashMap<String, String> buildFeatureParameters,
    @NotNull final HashMap<String, String> configParameters,
    @Nullable final Settings expectedSettings) throws IOException {
    // Given
    myCtx.checking(new Expectations() {{
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

      allowing(myRunnerParametersService).tryGetRunnerParameter(with(any(String.class)));
      will(new CustomAction("tryGetRunnerParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return runnerParameters.get(name);
        }
      });

      allowing(myBuildFeatureParametersService).getBuildFeatureParameters(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("getBuildFeatureParameters") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          Assert.assertEquals(invocation.getParameter(0), Constants.BUILD_FEATURE_TYPE);
          final String name = (String)invocation.getParameter(1);
          final String val = buildFeatureParameters.get(name);
          if(val != null) {
            return Arrays.asList(buildFeatureParameters.get(name));
          }

          return Arrays.asList();
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
      myRunnerParametersService,
      myBuildFeatureParametersService,
      myCommandLineArgumentsService);
  }
}
