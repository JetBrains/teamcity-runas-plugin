package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import org.jetbrains.annotations.NotNull;

public class FileAccessParser implements TextParser<AccessControlList> {
  private static final Pattern OutAccessPattern = Pattern.compile("\\s*([rcua\\s]+)\\s*([\\+\\-rwx\\s]+)\\s*,(.+)", Pattern.CASE_INSENSITIVE);
  private final PathMatcher myPathMatcher;
  private final PathsService myPathsService;
  private final FileService myFileService;

  public FileAccessParser(
    @NotNull final PathMatcher pathMatcher,
    @NotNull final PathsService pathsService,
    @NotNull final FileService fileService) {
    myPathMatcher = pathMatcher;
    myPathsService = pathsService;
    myFileService = fileService;
  }

  @NotNull
  @Override
  public AccessControlList parse(@NotNull final String acl) {
    final ArrayList<AccessControlEntry> accessControlEntries = new ArrayList<AccessControlEntry>();
    if(StringUtil.isEmptyOrSpaces(acl)) {
      return new AccessControlList(accessControlEntries);
    }

    final File agentBinDirectory = myPathsService.getPath(WellKnownPaths.Bin);
    final String[] entries = acl.split(";");
    for (String aclEntryStr: entries) {
      final Matcher aclMatch = OutAccessPattern.matcher(aclEntryStr);
      if(!aclMatch.find() || aclMatch.groupCount() != 3) {
        throw new BuildStartException(String.format("Invalid ACL specification \"%s\"", aclEntryStr));
      }

      final String targetStr = aclMatch.group(1).toLowerCase().trim();
      final String permissionsStr = aclMatch.group(2).toLowerCase().trim();
      final String antPatternsStr = aclMatch.group(3).trim();

      final EnumSet<AccessPermissions> permissions = EnumSet.noneOf(AccessPermissions.class);
      AccessControlAccount account = null;
      for(char targetChar: targetStr.toCharArray()) {
        switch (targetChar) {
          case ' ':
            break;

          case 'r':
            permissions.add(AccessPermissions.Recursive);
            break;

          case 'c':
            account = AccessControlAccount.forCurrent();
            break;

          case 'u':
            account = AccessControlAccount.forUser("");
            break;

          case 'a':
            account = AccessControlAccount.forAll();
            break;

          default:
            throw new BuildStartException(String.format("Invalid ACL specification \"%s\"", aclEntryStr));
        }
      }

      if(account == null) {
        throw new BuildStartException(String.format("Invalid ACL specification \"%s\"", aclEntryStr));
      }

      for(char permissionChar: permissionsStr.toCharArray()) {
        switch (permissionChar) {
          case ' ':
          case '+':
            break;

          case '-':
            permissions.add(AccessPermissions.Revoke);
            break;

          case 'r':
            permissions.add(AccessPermissions.AllowRead);
            break;

          case 'w':
            permissions.add(AccessPermissions.AllowWrite);
            break;

          case 'x':
            permissions.add(AccessPermissions.AllowExecute);
            break;

          default:
            throw new BuildStartException(String.format("Invalid ACL specification \"%s\"", aclEntryStr));
        }
      }

      final String[] antPatterns = antPatternsStr.split(",");
      final ArrayList<String> include = new ArrayList<String>();
      final ArrayList<String> exclude = new ArrayList<String>();

      for (String antPatternStr: antPatterns) {
        antPatternStr = antPatternStr.trim();
        if(antPatternStr.length() == 0) {
          continue;
        }

        final char firstChar = antPatternStr.charAt(0);
        switch (firstChar) {
          case '+':
            final String includePattern = antPatternStr.substring(1, antPatternStr.length()).trim();
            if(includePattern.length() > 0) {
              include.add(includePattern);
            }
            break;

          case '-':
            final String excludePattern = antPatternStr.substring(1, antPatternStr.length()).trim();
            if(excludePattern.length() > 0) {
              exclude.add(excludePattern);
            }
            break;

          default:
            if(antPatternStr.length() > 0) {
              include.add(antPatternStr);
            }
            break;
        }
      }

      final List<File> files = myPathMatcher.scanFiles(agentBinDirectory, include.toArray(new String[include.size()]), exclude.toArray(new String[exclude.size()]));
      if(files.size() > 0) {
        for (File file : files) {
          accessControlEntries.add(new AccessControlEntry(file, account, permissions));
        }
      }
      else {
        for (String includeItem: include) {
          final File includeFile = new File(includeItem);
          if (myFileService.exists(includeFile)) {
            accessControlEntries.add(new AccessControlEntry(includeFile, account, permissions));
          }
        }
      }
    }

    return new AccessControlList(accessControlEntries);
  }
}
