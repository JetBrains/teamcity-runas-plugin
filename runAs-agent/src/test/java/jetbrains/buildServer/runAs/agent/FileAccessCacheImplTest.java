package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class FileAccessCacheImplTest {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private Mockery myCtx;
  private Converter<String, String> myArgumentConverter;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    //noinspection unchecked
    myArgumentConverter = (Converter<String, String>)myCtx.mock(Converter.class);
  }

  @DataProvider(name = "cacheAccessControlEntryCases")
  public Object[][] getCacheAccessControlEntryCases() {
    return new Object[][] {
        // for single element
        {
          Arrays.asList(
            createAce("my_file")
          ),
          Arrays.asList(
            true
          )
        },

      // for dup element
      {
        Arrays.asList(
          createAce("my_file"),
          createAce("my_file"),
          createAce("my_file")
        ),
        Arrays.asList(
          true,
          false,
          false
        )
      },

      // for dup element with reset
      {
        Arrays.asList(
          createAce("my_file"),
          createAce("my_file"),
          null,
          createAce("my_file"),
          createAce("my_file")
        ),
        Arrays.asList(
          true,
          false,
          true,
          false
        )
      },

      // for dif element
      {
        Arrays.asList(
          createAce("my_file"),
          createAce("my_file"),
          createAce("my_file2"),
          createAce("my_file"),
          createAce("my_file"),
          createAce("my_file2")
        ),
        Arrays.asList(
          true,
          false,
          true,
          false,
          false,
          false
        )
      },

      // for dif element with reset
      {
        Arrays.asList(
          createAce("my_file"),
          createAce("my_file"),
          createAce("my_file2"),
          createAce("my_file"),
          createAce("my_file"),
          createAce("my_file2"),
          null,
          createAce("my_file2"),
          createAce("my_file"),
          createAce("my_file")
        ),
        Arrays.asList(
          true,
          false,
          true,
          false,
          false,
          false,
          true,
          true,
          false
        )
      },
    };
  }

  @Test(dataProvider = "cacheAccessControlEntryCases")
  public void shouldCacheAccessControlEntry(
      @NotNull final Iterable<AccessControlEntry> accessControlEntries,
      @NotNull final Iterable<Boolean> expectedResults) {
    // Given
    final FileAccessCacheImpl instance = createInstance();
    final ArrayList<Boolean> actualResults = new ArrayList<Boolean>();

    // When
    for(AccessControlEntry ace: accessControlEntries) {
      if(ace == null) {
        instance.reset();
        continue;
      }

      actualResults.add(instance.tryAddEntry(ace));
    }

    // Then
    then(actualResults).isEqualTo(expectedResults);
  }

  @NotNull
  private FileAccessCacheImpl createInstance()
  {
    return new FileAccessCacheImpl();
  }

  private AccessControlEntry createAce(@NotNull final String file) {
    return new AccessControlEntry(new File(file), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite));
  }
}
