package jetbrains.buildServer.runAs.agent;

import java.io.IOException;
import java.util.HashMap;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
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

public class ParametersServiceTest{
  private Mockery myCtx;
  private RunnerParametersService myRunnerParametersService;
  private BuildFeatureParametersService myBuildFeatureParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myBuildFeatureParametersService = myCtx.mock(BuildFeatureParametersService.class);
  }

  @DataProvider(name = "getParamCases")
  public Object[][] getParamCases() {
    return new Object[][] {
      // from buildFeature
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user2"
      },

      // from buildFeature when UI was enabled
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user2"
      },

      // from buildFeature when UI was enabled
      {
        new HashMap<String, String>() {{ put(Constants.USER, "user1"); put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user1"
      },

      // from runnerParameters
      {
        new HashMap<String, String>() {{ put(Constants.USER, "user1"); put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user1"
      },

      // from configParameters
      {
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "true");}},
        Constants.USER,
        "user3"
      },

      // from runnerParameters
      {
        new HashMap<String, String>() {{ put(Constants.USER, "user1"); put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user1"
      },

      // from buildFeature
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user2"
      },

      // from runnerParameters
      {
        new HashMap<String, String>() {{ put(Constants.USER, "user1"); put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>(),
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "true"); }},
        Constants.USER,
        "user1"
      },

      // from configParameters when UI was disabled
      {
        new HashMap<String, String>() {{ put(Constants.USER, "user1"); put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); put(Constants.RUN_AS_UI_ENABLED, "false"); }},
        Constants.USER,
        "user3"
      },

      // from configParameters when UI by default
      {
        new HashMap<String, String>() {{ put(Constants.USER, "user1"); put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user2"); put(Constants.PASSWORD, "password2"); }},
        new HashMap<String, String>() {{ put(Constants.USER, "user3"); put(Constants.PASSWORD, "password3"); }},
        Constants.USER,
        "user3"
      },
    };
  }

  @Test(dataProvider = "getParamCases")
  public void shouldGetParam(
    @NotNull final HashMap<String, String> runnerParameters,
    @NotNull final HashMap<String, String> buildFeatureParameters,
    @NotNull final HashMap<String, String> configParameters,
    @NotNull final String parameterName,
    @Nullable final String expectedValue) throws IOException {
    // Given
    myCtx.checking(new Expectations() {{
      allowing(myRunnerParametersService).tryGetRunnerParameter(with(any(String.class)));
      will(new CustomAction("tryGetRunnerParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return runnerParameters.get(name);
        }
      });

      allowing(myBuildFeatureParametersService).tryGetBuildFeatureParameter(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("tryGetBuildFeatureParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          Assert.assertEquals(invocation.getParameter(0), Constants.BUILD_FEATURE_TYPE);
          final String name = (String)invocation.getParameter(1);
          final String val = buildFeatureParameters.get(name);
          if(val != null) {
            return buildFeatureParameters.get(name);
          }

          return null;
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

    final ParametersService provider = createInstance();

    // When
    final String actualValue = provider.tryGetParameter(parameterName);

    // Then
    myCtx.assertIsSatisfied();
    then(actualValue).isEqualTo(expectedValue);
  }

  @NotNull
  private ParametersService createInstance()
  {
    return new ParametersServiceImpl(
      myRunnerParametersService,
      myBuildFeatureParametersService);
  }
}
