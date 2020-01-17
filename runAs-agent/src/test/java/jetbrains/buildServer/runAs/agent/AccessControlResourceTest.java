/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import java.util.Arrays;
import java.util.EnumSet;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccessControlResourceTest {
  private Mockery myCtx;
  private FileAccessService myFileAccessService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileAccessService = myCtx.mock(FileAccessService.class);
  }

  @Test()
  public void shouldSetAccess() throws IOException {
    // Given
    final AccessControlEntry ace = new AccessControlEntry(new File("file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step);
    final AccessControlList acl = new AccessControlList(Arrays.asList(ace));
    final CommandLineExecutionContext executionContext = new CommandLineExecutionContext(0);
    myCtx.checking(new Expectations() {{
      oneOf(myFileAccessService).setAccess(acl);
    }});

    final AccessControlResource instance = createInstance();
    instance.setAcl(acl);

    // When
    instance.publishBeforeBuild(executionContext);

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private AccessControlResource createInstance()
  {
    return new AccessControlResourceImpl(myFileAccessService);
  }
}
