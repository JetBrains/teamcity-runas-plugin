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

import java.util.ArrayList;
import java.util.Arrays;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsSetupBuilderTest {

  private Mockery myCtx;
  private Environment myEnvironment;
  private CommandLineSetupBuilder myRunAsWindowsSetupBuilder;
  private CommandLineSetupBuilder myRunAsUnixSetupBuilder;
  private CommandLineSetupBuilder myRunAsMacSetupBuilder;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myEnvironment = myCtx.mock(Environment.class);
    myRunAsWindowsSetupBuilder = myCtx.mock(CommandLineSetupBuilder.class, "RunAsPlatformSpecificSetupBuilder");
    myRunAsUnixSetupBuilder = myCtx.mock(CommandLineSetupBuilder.class, "RunAsUnixSetupBuilder");
    myRunAsMacSetupBuilder = myCtx.mock(CommandLineSetupBuilder.class, "RunAsMacSetupBuilder");
  }

  @Test()
  public void shouldBuildSetupWhenWindows()
  {
    // Given
    final CommandLineSetup commandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());

    myCtx.checking(new Expectations() {{
        oneOf(myEnvironment).getOperationSystem();
        will(returnValue(OperationSystem.Windows));

        oneOf(myRunAsWindowsSetupBuilder).build(commandLineSetup);
        will(returnValue(Arrays.asList(runAsCommandLineSetup)));

        never(myRunAsUnixSetupBuilder).build(commandLineSetup);
      never(myRunAsMacSetupBuilder).build(commandLineSetup);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(runAsCommandLineSetup);
  }

  @Test()
  public void shouldBuildSetupWhenLinux()
  {
    // Given
    final CommandLineSetup commandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());

    myCtx.checking(new Expectations() {{
      oneOf(myEnvironment).getOperationSystem();
      will(returnValue(OperationSystem.Other));

      oneOf(myRunAsUnixSetupBuilder).build(commandLineSetup);
      will(returnValue(Arrays.asList(runAsCommandLineSetup)));

      never(myRunAsWindowsSetupBuilder).build(commandLineSetup);
      never(myRunAsMacSetupBuilder).build(commandLineSetup);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(runAsCommandLineSetup);
  }

  @Test()
  public void shouldBuildSetupWhenMac()
  {
    // Given
    final CommandLineSetup commandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());

    myCtx.checking(new Expectations() {{
      oneOf(myEnvironment).getOperationSystem();
      will(returnValue(OperationSystem.Mac));

      oneOf(myRunAsMacSetupBuilder).build(commandLineSetup);
      will(returnValue(Arrays.asList(runAsCommandLineSetup)));

      never(myRunAsWindowsSetupBuilder).build(commandLineSetup);
      never(myRunAsUnixSetupBuilder).build(commandLineSetup);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(runAsCommandLineSetup);
  }

  private CommandLineSetupBuilder createInstance()
  {
    return new RunAsSetupBuilder(
        myEnvironment,
        myRunAsWindowsSetupBuilder,
        myRunAsUnixSetupBuilder,
        myRunAsMacSetupBuilder
      );
  }
}
