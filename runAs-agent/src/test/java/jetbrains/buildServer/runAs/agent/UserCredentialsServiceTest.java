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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class UserCredentialsServiceTest {
  private final AccessControlList myAccessControlList;
  private Mockery myCtx;
  private ParametersService myParametersService;
  private ProfileParametersService myProfileParametersService;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private TextParser<AccessControlList> myFileAccessParser;
  private AgentParametersService myAgentParametersService;

  public UserCredentialsServiceTest() {
    myAccessControlList = new AccessControlList(Arrays.asList(new AccessControlEntry(new File("file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.Recursive), AccessControlScope.Step)));
  }

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myAgentParametersService = myCtx.mock(AgentParametersService.class);
    myParametersService = myCtx.mock(ParametersService.class);
    myProfileParametersService = myCtx.mock(ProfileParametersService.class);
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
    //noinspection unchecked
    myFileAccessParser = (TextParser<AccessControlList>)myCtx.mock(TextParser.class);
  }

  @DataProvider(name = "getUserCredentialsCases")
  public Object[][] getUserCredentialsCases() {
    return new Object[][] {
      // Default && ret null
      {
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new HashMap<String, HashMap<String, String>>(),
        null,
        null
      },

      // Predefined && ret null
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
        }},
        new HashMap<String, HashMap<String, String>>(),
        null,
        "RunAs user must be defined for \"" + UserCredentialsServiceImpl.DEFAULT_PROFILE + "\""
      },

      // Predefined credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.CONFIG_PASSWORD, "password2");
          }});
        }},
        new UserCredentials("user2cred", "user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Predefined credentials by absolute path
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.PASSWORD, "password2");
          }});
        }},
        new UserCredentials("user2cred", "user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Predefined credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.CONFIG_PASSWORD, "password2");
          }});
        }},
        new UserCredentials("user2cred", "user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // PredefinedCredentials && default credentials
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
            put(Constants.USER, "user3");
            put(Constants.PASSWORD, "password3");
          }});
        }},
        new UserCredentials("default", "user3", "password3", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // predefined credentials && throw exception WHEN password is empty
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.PASSWORD, "");
          }});
        }},
        null,
        "RunAs password must be defined for"
      },

      // predefined credentials && throw exception WHEN password is null
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
          }});
        }},
        null,
        "RunAs password must be defined for"
      },

      // predefined credentials WHEN throw exception when user is empty
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "");
            put(Constants.PASSWORD, "password2");
          }});
        }},
        null,
        "RunAs user must be defined for"
      },

      // PredefinedCredentials && predefined credentials WHEN throw exception when user is null
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.PASSWORD, "password2");
          }});
        }},
        null,
        "RunAs user must be defined for"
      },

      // PredefinedCredentials && use default credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
            put(Constants.USER, "user4");
            put(Constants.CONFIG_PASSWORD, "password4");
          }});
        }},
        new UserCredentials("default", "user4", "password4", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Enabled by default && custom credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>(),
        new HashMap<String, HashMap<String, String>>(),
        new UserCredentials("", "user1", "password1", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Enabled && custom credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
          }},
        new HashMap<String, HashMap<String, String>>(),
        new UserCredentials("", "user1", "password1", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Enabled && predefined credentials WHEN custom credentials is not defined
      {
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");}},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.PASSWORD, "password2");
          }});
        }},
        new UserCredentials("user2cred", "user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Enabled && predefined credentials && ret null WHEN credentials is not defined at all
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user55");
            put(Constants.PASSWORD, "password55");
          }});
        }},
        null,
        null
      },

      // PredefinedCredentials && ignore custom credentials
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.PASSWORD, "password2");
          }});
        }},
        new UserCredentials("user2cred", "user2", "password2", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Disabled && ret null
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "false");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user66");
            put(Constants.PASSWORD, "password66");
          }});
        }},
        null,
        null
      },

      // Disabled && ret null
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user99");
          put(Constants.PASSWORD, "password99");
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "false");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "false");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");
        }},
        new HashMap<String, String>(),
        null,
        null
      },

      // Enabled && custom credentials && additional params
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
          put(Constants.ADDITIONAL_ARGS, "arg1 arg2");
          put(Constants.WINDOWS_INTEGRITY_LEVEL, WindowsIntegrityLevel.High.getValue());
          put(Constants.LOGGING_LEVEL, LoggingLevel.Debug.getValue());
        }},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>(),
        new UserCredentials("", "user1", "password1", WindowsIntegrityLevel.High, LoggingLevel.Debug, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER))),
        null
      },

      // Enabled && predefined credentials && additional params WHEN custom credentials is not defined
      {
        new HashMap<String, String>() {{
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");}},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
          put(Constants.USER, "user2");
          put(Constants.PASSWORD, "password2");
          put(Constants.ADDITIONAL_ARGS, "arg1 arg2");
          put(Constants.WINDOWS_INTEGRITY_LEVEL, WindowsIntegrityLevel.High.getValue());
          put(Constants.LOGGING_LEVEL, LoggingLevel.Debug.getValue());
          }});
        }},
        new UserCredentials("user2cred", "user2", "password2", WindowsIntegrityLevel.High, LoggingLevel.Debug, Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER))),
        null
      },

      // Enabled && default credentials && update username in ACL
      {
        new HashMap<String, String>(),
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
          put(Constants.USER, "user78");
          put(Constants.PASSWORD, "password78");
          put(Constants.ADDITIONAL_ARGS, "arg1 arg2");
          put(Constants.WINDOWS_INTEGRITY_LEVEL, WindowsIntegrityLevel.High.getValue());
          put(Constants.LOGGING_LEVEL, LoggingLevel.Debug.getValue());
          put(Constants.RUN_AS_ACL, "acl");
          }});
        }},
        new UserCredentials(
          "default",
          "user78",
          "password78",
          WindowsIntegrityLevel.High,
          LoggingLevel.Debug,
          Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER))),
        null
      },

      // ALLOW_PROFILE_ID_FROM_SERVER is false by default
      {
        new HashMap<String, String>(),
        new HashMap<String, String>(),
        new HashMap<String, HashMap<String, String>>() {{
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
            put(Constants.USER, "user3");
            put(Constants.PASSWORD, "password3");
          }});
        }},
        null,
        null
      },

      // ALLOW_CUSTOM_CREDENTIALS is true by default
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1"); }},
        new HashMap<String, String>(),
        new HashMap<String, HashMap<String, String>>(),
        new UserCredentials("", "user1", "password1", WindowsIntegrityLevel.Auto, LoggingLevel.Off, Arrays.<CommandLineArgument>asList()),
        null
      },

      // Throw exception WHEN predefined credentials was enabled and defined but custom credentials is defined too
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");}},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.PASSWORD, "password2");
          }});
        }},
        null,
        "Build step cannot be executed with custom credentials on this agent. Please contact system administrator."
      },

      // Throw exception WHEN predefined credentials was enabled but default is specified only and defined but custom credentials is defined too
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred");}},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          // default
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
            put(Constants.USER, "user3");
            put(Constants.PASSWORD, "password3");
          }});
        }},
        null,
        "Build step cannot be executed with custom credentials on this agent. Please contact system administrator."
      },

      // Throw exception WHEN predefined credentials was enabled but default is specified only and defined but custom credentials is defined too and credential profile was not specified
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");}},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          // default
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
            put(Constants.USER, "user3");
            put(Constants.PASSWORD, "password3");
          }});
        }},
        null,
        "Build step cannot be executed with custom credentials on this agent. Please contact system administrator."
      },

      // Throw exception WHEN predefined credentials is enabled, but it was nod used and default is specified  and defined but custom credentials is defined too
      {
        new HashMap<String, String>() {{
          put(Constants.USER, "user1");
          put(Constants.PASSWORD, "password1");
          put(Constants.CREDENTIALS_PROFILE_ID, "user2cred_AAAAA");}},
        new HashMap<String, String>() {{
          put(Constants.ALLOW_PROFILE_ID_FROM_SERVER, "true");
          put(Constants.ALLOW_CUSTOM_CREDENTIALS, "true");
        }},
        new HashMap<String, HashMap<String, String>>() {{
          // was not used
          put("user2cred", new HashMap<String, String>() {{
            put(Constants.USER, "user2");
            put(Constants.PASSWORD, "password2");
          }});

          // default
          put(UserCredentialsServiceImpl.DEFAULT_PROFILE, new HashMap<String, String>() {{
            put(Constants.USER, "user3");
            put(Constants.PASSWORD, "password3");
          }});
        }},
        null,
        "Build step cannot be executed with custom credentials on this agent. Please contact system administrator."
      },
    };
  }

  @Test(dataProvider = "getUserCredentialsCases")
  public void shouldGetUserCredentials(
    @NotNull final HashMap<String, String> parameters,
    @NotNull final HashMap<String, String> configParameters,
    @NotNull final HashMap<String, HashMap<String, String>> properties,
    @Nullable final UserCredentials expectedUserCredentials,
    @Nullable final String expectedExceptionMessage) throws IOException {

    // Given
    myCtx.checking(new Expectations() {{
      allowing(myAgentParametersService).tryGetConfigParameter(with(any(String.class)));
      will(returnValue(null));

      allowing(myParametersService).tryGetParameter(with(any(String.class)));
      will(new CustomAction("tryGetParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return parameters.get(name);
        }
      });

      allowing(myParametersService).tryGetConfigParameter(with(any(String.class)));
      will(new CustomAction("tryGetConfigParameter") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final String name = (String)invocation.getParameter(0);
          return configParameters.get(name);
        }
      });

      allowing(myProfileParametersService).tryGetProperty(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("tryGetProperty") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          final HashMap<String, String> propSet = properties.get((String)invocation.getParameter(0));
          if(propSet == null) {
            return null;
          }
          //noinspection SuspiciousMethodCalls
          return propSet.get(invocation.getParameter(1));
        }
      });

      allowing(myFileAccessParser).parse("acl");
      will(returnValue(myAccessControlList));

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

    final UserCredentialsService userCredentialsService = createInstance();
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
  private UserCredentialsService createInstance()
  {
    return new UserCredentialsServiceImpl(
      myParametersService,
      myProfileParametersService,
      myCommandLineArgumentsService);
  }
}
