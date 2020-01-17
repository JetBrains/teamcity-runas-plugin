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

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.LoggerService;
import jetbrains.buildServer.messages.serviceMessages.Message;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class ScopedFileAccessServiceTest {
  private Mockery myCtx;
  private FileAccessService myFileAccessService;
  private MyFileAccessCache myGlobalFileAccessCache;
  private MyFileAccessCache myBuildFileAccessCache;
  private LoggerService myLoggerService;
  private AccessControlEntry myAce;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileAccessService = myCtx.mock(FileAccessService.class);
    myLoggerService = myCtx.mock(LoggerService.class);
    myGlobalFileAccessCache = new MyFileAccessCache();
    myBuildFileAccessCache = new MyFileAccessCache();
    myAce = new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step);
  }

  @DataProvider(name = "getSetPermissionsCases")
  public Object[][] getSetPermissionsCases() {
    return new Object[][] {
      // Use different caches
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global)))
        ),
        1,
        1
      },

      // Use different caches in one operation
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global),
            createAce("my_file", AccessControlScope.Build)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global),
            createAce("my_file", AccessControlScope.Build)))
        ),
        1,
        1
      },

      // Use different caches in one operation
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global),
            createAce("my_file", AccessControlScope.Build),
            createAce("my_file", AccessControlScope.Step)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global),
            createAce("my_file", AccessControlScope.Build),
            createAce("my_file", AccessControlScope.Step)))
        ),
        1,
        1
      },

      // First set and caching is enabled and caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global)))
        ),
        1,
        0
      },

      // First set and caching is enabled and caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build)))
        ),
        0,
        1
      },

      // Second set and caching is enabled and caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build)))
        ),
        0,
        1
      },

      // Second set amd mult elements and caching is enabled and caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Build),
            createAce("my_file", AccessControlScope.Build)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Build)))
        ),
        0,
        2
      },

      // Second set amd mult elements and caching is enabled and caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Build),
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build),
            createAce("my_file2", AccessControlScope.Build)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Build)))
        ),
        0,
        2
      },

      // Second set amd mult elements and caching is enabled and caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Global),
            createAce("my_file", AccessControlScope.Global))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global),
            createAce("my_file2", AccessControlScope.Global)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Global))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Global)))
        ),
        2,
        0
      },

      // Second set and caching is enabled and not caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step)))
        ),
        0,
        0
      },

      // Second set and caching is enabled and caching and not caching element
      {
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step),
            createAce("my_file2", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", AccessControlScope.Build),
            createAce("my_file", AccessControlScope.Step)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step),
            createAce("my_file2", AccessControlScope.Build))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", AccessControlScope.Step)))
        ),
        0,
        1
      },
    };
  }

  @Test(dataProvider = "getSetPermissionsCases")
  public void shouldSetPermissions(
    @NotNull final Iterable<AccessControlList> accessControlLists,
    @NotNull final Iterable<AccessControlList> expectedAccessControlLists,
    final int expectedGlobalCacheSize,
    final int expectedBuildCacheSize) throws ExecutionException {
    // Given
    final List<AccessControlList> actualAccessControlLists = new ArrayList<AccessControlList>();
    final FileAccessService instance = createInstance();

    myCtx.checking(new Expectations() {{
      allowing(myLoggerService).onMessage(with(any(ServiceMessage.class)));

      allowing(myFileAccessService).setAccess(with(any(AccessControlList.class)));
      will(new CustomAction("setAccess") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          actualAccessControlLists.add((AccessControlList)invocation.getParameter(0));
          return Lists.emptyList();
        }
      });
    }});

    // When
    for (AccessControlList acl: accessControlLists) {
      instance.setAccess(acl);
    }

    // Then
    myCtx.assertIsSatisfied();
    then(actualAccessControlLists).isEqualTo(expectedAccessControlLists);
    then(myGlobalFileAccessCache.size()).isEqualTo(expectedGlobalCacheSize);
    then(myBuildFileAccessCache.size()).isEqualTo(expectedBuildCacheSize);
  }

  @Test
  public void shouldShowGrantingPermMessage() throws ExecutionException {
    // Given
    final FileAccessService instance = createInstance();

    myCtx.checking(new Expectations() {{
      oneOf(myLoggerService).onMessage(with(new BaseMatcher<Message>() {
                                              @Override
                                              public boolean matches(final Object o) {
                                                return "NORMAL".equals(((Message)o).getStatus()) && ScopedFileAccessService.MESSAGE_GRANTING_PERMISSIONS.equals(((Message)o).getText());
                                              }

                                              @Override
                                              public void describeTo(final Description description) {
                                              }
                                            }));

      allowing(myFileAccessService).setAccess(with(any(AccessControlList.class)));
      will(returnValue(Lists.emptyList()));
    }});

    // When
    instance.setAccess(new AccessControlList(Arrays.asList(createAce("my_file", AccessControlScope.Build))));

    // Then
    myCtx.assertIsSatisfied();
  }

  @SuppressWarnings("unchecked")
  @DataProvider(name = "warnCases")
  public Object[][] getWarnCases() {
    return new Object[][] {
      { Arrays.asList(new Result<AccessControlEntry, Boolean>(myAce, false)) },
      { Arrays.asList(new Result<AccessControlEntry, Boolean>(myAce, new Exception("aa"))) },
      { Arrays.asList(new Result<AccessControlEntry, Boolean>(myAce, true), new Result<AccessControlEntry, Boolean>(myAce, false)) },
      { Arrays.asList(new Result<AccessControlEntry, Boolean>(myAce, new Exception("aa")), new Result<AccessControlEntry, Boolean>(myAce, true), new Result<AccessControlEntry, Boolean>(myAce, false)) },
    };
  }

  @Test(dataProvider = "warnCases")
  public void shouldShowWarningMessage(
    @NotNull final List<Result<AccessControlEntry, Boolean>> results) throws ExecutionException {
    // Given
    final FileAccessService instance = createInstance();

    myCtx.checking(new Expectations() {{
      one(myLoggerService).onMessage(with(any(Message.class)));

      oneOf(myLoggerService).onMessage(with(new BaseMatcher<Message>() {
        @Override
        public boolean matches(final Object o) {
          return "WARNING".equals(((Message)o).getStatus()) && ScopedFileAccessService.WARNING_PERMISSIONS_ERRORS.equals(((Message)o).getText());
        }

        @Override
        public void describeTo(final Description description) {
        }
      }));

      allowing(myFileAccessService).setAccess(with(any(AccessControlList.class)));
      will(returnValue(results));
    }});

    // When
    instance.setAccess(new AccessControlList(Arrays.asList(createAce("my_file", AccessControlScope.Build))));

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private FileAccessService createInstance()
  {
    return new ScopedFileAccessService(
      myFileAccessService,
      myLoggerService,
      myGlobalFileAccessCache,
      myBuildFileAccessCache);
  }

  private AccessControlEntry createAce(@NotNull final String file, final AccessControlScope scope) {
    return new AccessControlEntry(new File(file), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), scope);
  }

  private static class MyFileAccessCache implements FileAccessCache {
    private final HashSet<AccessControlEntry> myAcls = new HashSet<AccessControlEntry>();

    int size() {
      return myAcls.size();
    }

    @Override
    public boolean tryAddEntry(@NotNull final AccessControlEntry acl) {
      return myAcls.add(acl);
    }
  }
}
