package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class ScopedAccessServiceTest {
  private Mockery myCtx;
  private FileAccessService myFileAccessService;
  private MyFileAccessCache myGlobalFileAccessCache;
  private MyFileAccessCache myBuildFileAccessCache;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileAccessService = myCtx.mock(FileAccessService.class);
    myGlobalFileAccessCache = new MyFileAccessCache();
    myBuildFileAccessCache = new MyFileAccessCache();
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
      allowing(myFileAccessService).setAccess(with(any(AccessControlList.class)));
      will(new CustomAction("setAccess") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          actualAccessControlLists.add((AccessControlList)invocation.getParameter(0));
          return null;
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

  @NotNull
  private FileAccessService createInstance()
  {
    return new ScopedFileAccessService(
      myFileAccessService,
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
