package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class FileAccessParserTest {
  private Mockery myCtx;
  private PathMatcher myPathMatcher;
  private PathsService myPathsService;
  private File myAgentHomeDirectory;
  private FileService myFileService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPathMatcher = myCtx.mock(PathMatcher.class);
    myPathsService = myCtx.mock(PathsService.class);
    myFileService = myCtx.mock(FileService.class);
    myAgentHomeDirectory = new File("AgentHome");
  }

  @DataProvider(name = "parseAclCases")
  public Object[][] getParseAclCasesCases() {
    return new Object[][] {
      {
        "rc+rwx,+dirIncl,-dirExcl",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"), new File("dir2"))
          )),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // with spaces
      {
        " r c + r wx , + dirIncl , - dirExcl ",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"), new File("dir2"))
          )),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // for all users
      {
        "ru+rwx,+dirIncl,-dirExcl",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"), new File("dir2"))
          )),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // for all users non recursive
      {
        "u+rwx,+dirIncl,-dirExcl",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"), new File("dir2"))
          )),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute))
        )),
        false},

      // several acl entries
      {
        "rc+rwx,+dirIncl,-dirExcl;rc+rwx,dirIncl2",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"))
          ),
          new PathMatcherData(
            new String[] {"dirIncl2"},
            new String[] {},
            Arrays.asList(new File("dir2"))
          )
        ),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // several acl entries and spaces
      {
        "rc+rwx,+dirIncl,- dirExcl   ;   rc+rw x , dirIncl2",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"))
          ),
          new PathMatcherData(
            new String[] {"dirIncl2"},
            new String[] {},
            Arrays.asList(new File("dir2"))
          )
        ),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // include anr pattern without +
      {
        "rc+rwx,dirIncl,-dirExcl",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.asList(new File("dir1"), new File("dir2"))
          )),
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // no files matched
      {
        "rc+rwx,+dirIncl,-dirExcl",
        Arrays.asList(
          new PathMatcherData(
            new String[] {"dirIncl"},
            new String[] {"dirExcl"},
            Arrays.<File>asList()
          )),
        new AccessControlList(Arrays.<AccessControlEntry>asList()),
        false},

      //invalid user spec
      {
        "rcz+rwx,+dirIncl,-dirExcl",
        Arrays.asList(),
        null,
        true},

      //invalid access spec
      {
        "rc+rwz,+dirIncl,-dirExcl",
        Arrays.asList(),
        null,
        true},

      //no ant pattern spec
      {
        "rc+rw",
        Arrays.asList(),
        null,
        true},

      //invalid spec
      {
        "abc",
        Arrays.asList(),
        null,
        true},

      //empty spec
      {
        "",
        Arrays.asList(),
        new AccessControlList(Arrays.<AccessControlEntry>asList()),
        false},
    };
  }

  @Test(dataProvider = "parseAclCases")
  public void shouldParseAcl(
    @NotNull final String aclStr,
    @NotNull final List<PathMatcherData> pathMatcherData,
    @Nullable final AccessControlList expectedAcl,
    final boolean expectedThrowException) throws ExecutionException {
    // Given
    final TextParser<AccessControlList> instance = createInstance();

    myCtx.checking(new Expectations() {{
      oneOf(myPathsService).getPath(WellKnownPaths.Bin);
      will(returnValue(myAgentHomeDirectory));

      for(PathMatcherData pathMatcherItem: pathMatcherData) {
        oneOf(myPathMatcher).scanFiles(myAgentHomeDirectory, pathMatcherItem.getIncludeRules(), pathMatcherItem.getExcludeRules());
        will(returnValue(pathMatcherItem.getResult()));
      }
    }});

    boolean actualThrowException = false;
    AccessControlList actualAcl = null;
    // When
    try {
      actualAcl = instance.parse(aclStr);
    }
    catch (BuildStartException buildStartException) {
      actualThrowException = true;
    }

    // Then
    then(actualThrowException).isEqualTo(expectedThrowException);
    then(actualAcl).isEqualTo(expectedAcl);
  }

  @NotNull
  private TextParser<AccessControlList> createInstance()
  {
    return new FileAccessParser(
      myPathMatcher,
      myPathsService,
      myFileService);
  }

  private static class PathMatcherData {
    private final String[] myIncludeRules;
    private final String[] myExcludeRules;
    private final List<File> myResult;

    public PathMatcherData(
      @NotNull final String[] includeRules,
      @NotNull final String[] excludeRules,
      @NotNull final List<File> result) {
      myIncludeRules = includeRules;
      myExcludeRules = excludeRules;
      myResult = result;
    }

    @NotNull
    public String[] getIncludeRules() {
      return myIncludeRules;
    }

    @NotNull
    public String[] getExcludeRules() {
      return myExcludeRules;
    }

    @NotNull
    public List<File> getResult() {
      return myResult;
    }
  }
}
