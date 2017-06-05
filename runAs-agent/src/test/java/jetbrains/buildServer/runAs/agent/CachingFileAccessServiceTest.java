package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_ACL_CACHING_ENABLED;
import static org.assertj.core.api.BDDAssertions.then;

public class CachingFileAccessServiceTest {
  private Mockery myCtx;
  private FileAccessService myFileAccessService;
  private ParametersService myParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileAccessService = myCtx.mock(FileAccessService.class);
    myParametersService = myCtx.mock(ParametersService.class);
  }

  @DataProvider(name = "getSetPermissionsCases")
  public Object[][] getSetPermissionsCases() {
    return new Object[][] {
      // First set and caching is enabled and caching element
      {
        "true",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        )
      },

      // Second set and caching is enabled and caching element
      {
        "true",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        )
      },

      // Second set and caching is by default and caching element
      {
        null,
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        )
      },

      // Second set and caching is empty and caching element
      {
        "",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        )
      },

      // Second set amd mult elements and caching is enabled and caching element
      {
        "true",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", true),
            createAce("my_file", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", true)))
        )
      },

      // Second set amd mult elements and caching is enabled and caching element
      {
        "true",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", true),
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", true),
            createAce("my_file2", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", true)))
        )
      },

      // Second set and caching is disabled and caching element
      {
        "false",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", true)))
        )
      },

      // Second set and caching is enabled and not caching element
      {
        "true",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", false))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", false)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", false))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", false)))
        )
      },

      // Second set and caching is enabled and caching and not caching element
      {
        "true",
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", false),
            createAce("my_file2", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file2", true),
            createAce("my_file", false)))
        ),
        Arrays.asList(
          new AccessControlList(Arrays.asList(
            createAce("my_file", false),
            createAce("my_file2", true))),
          new AccessControlList(Arrays.asList(
            createAce("my_file", false)))
        )
      },
    };
  }

  @Test(dataProvider = "getSetPermissionsCases")
  public void shouldSetPermissions(
    @Nullable final String isCachingEnabled,
    @NotNull final Iterable<AccessControlList> accessControlLists,
    @NotNull final Iterable<AccessControlList> expectedAccessControlLists) throws ExecutionException {
    // Given
    final List<AccessControlList> actualAccessControlLists = new ArrayList<AccessControlList>();
    final FileAccessService instance = createInstance();
    final ArrayList<CommandLineSetup> actualCommandLineSetups = new ArrayList<CommandLineSetup>();

    myCtx.checking(new Expectations() {{
      allowing(myParametersService).tryGetConfigParameter(RUN_AS_ACL_CACHING_ENABLED);
      will(returnValue(isCachingEnabled));

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
  }

  @NotNull
  private FileAccessService createInstance()
  {
    return new CachingFileAccessService(
      myFileAccessService,
      new FileAccessCache() {
        private final HashSet<AccessControlEntry> myAcls = new HashSet<AccessControlEntry>();

        @Override
        public boolean tryAddEntry(@NotNull final AccessControlEntry acl) {
          return myAcls.add(acl);
        }
      },
      myParametersService);
  }

  private AccessControlEntry createAce(@NotNull final String file, final boolean isCachingAllowed) {
    final AccessControlEntry ace = new AccessControlEntry(new File(file), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite));
    if(isCachingAllowed) {
      ace.setCachingAllowed(true);
    }

    return ace;
  }
}
