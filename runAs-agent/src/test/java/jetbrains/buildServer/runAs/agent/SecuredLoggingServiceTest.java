/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecuredLoggingServiceTest {
  private Mockery myCtx;
  private BuildRunnerContextProvider myBuildRunnerContextProvider;
  private BuildRunnerContext myBuildRunnerContext;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myBuildRunnerContextProvider = myCtx.mock(BuildRunnerContextProvider.class);
    myBuildRunnerContext = myCtx.mock(BuildRunnerContext.class);
  }

  @Test
  public void shouldDisableLoggingOfCommandLine() {
    // Given
    myCtx.checking(new Expectations() {{
      oneOf(myBuildRunnerContextProvider).getContext();
      will(returnValue(myBuildRunnerContext));

      oneOf(myBuildRunnerContext).addConfigParameter(SecuredLoggingServiceImpl.TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE, Boolean.toString(false));
    }});

    final SecuredLoggingService service = createInstance();

    // When
    service.disableLoggingOfCommandLine();

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private SecuredLoggingService createInstance()
  {
    return new SecuredLoggingServiceImpl(
      myBuildRunnerContextProvider);
  }
}
