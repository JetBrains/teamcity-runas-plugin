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

import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class LinuxSettingsGeneratorTest {
  private Mockery myCtx;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
  }

  @Test()
  public void shouldGenerateContent() {
    // Given
    final String expectedContent = "nik";
    final List<CommandLineArgument> additionalArgs = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg 2", CommandLineArgument.Type.PARAMETER));

    final ResourceGenerator<UserCredentials> instance = createInstance();

    // When
    final String content = instance.create(new UserCredentials("", "nik", "aaa", WindowsIntegrityLevel.Auto, LoggingLevel.Off, additionalArgs));

    // Then
    myCtx.assertIsSatisfied();
    then(content.trim().replace("\n", " ").replace("\r", "")).isEqualTo(expectedContent.trim().replace("\n", " ").replace("\r", ""));
  }

  @NotNull
  private ResourceGenerator<UserCredentials> createInstance()
  {
    return new LinuxSettingsGenerator();
  }
}
