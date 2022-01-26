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

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.agent.ToolProvider;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsToolProviderTest {
  private Mockery myCtx;
  private ToolProvidersRegistry myToolProvidersRegistry;
  private PluginDescriptor myPluginDescriptor;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPluginDescriptor = myCtx.mock(PluginDescriptor.class);
    myToolProvidersRegistry = myCtx.mock(ToolProvidersRegistry.class);
  }

  @Test
  public void shouldGetPath() throws IOException, ExecutionException {
    // Given
    final File pluginRootDir = new File("plugin");
    final File binPath = new File(pluginRootDir, RunAsToolProvider.BIN_PATH);

    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myPluginDescriptor).getPluginRoot();
      will(returnValue(pluginRootDir));

      oneOf(myToolProvidersRegistry).registerToolProvider(with(any(ToolProvider.class)));
      will(new CustomAction("registerToolProvider") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          toolProviders.add((ToolProvider)invocation.getParameter(0));
          return null;
        }
      });
    }});

    // When
    createInstance();

    final ToolProvider toolProvider = toolProviders.get(0);
    final String toolPath = toolProvider.getPath(Constants.RUN_AS_TOOL_NAME);

    // Then
    myCtx.assertIsSatisfied();
    then(toolProviders.size()).isEqualTo(1);
    then(toolProvider.supports(Constants.RUN_AS_TOOL_NAME)).isTrue();
    then(new File(toolPath).getAbsolutePath()).isEqualTo(binPath.getAbsolutePath());
  }

  @NotNull
  private RunAsToolProvider createInstance()
  {
    return new RunAsToolProvider(
      myPluginDescriptor,
      myToolProvidersRegistry);
  }
}
