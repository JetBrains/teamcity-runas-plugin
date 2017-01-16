package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import org.jetbrains.annotations.NotNull;

public class FileAccessParser implements TextParser<AccessControlList> {
  private static final Pattern OutAccessPattern = Pattern.compile("\\s*([rcua\\s]+)\\s*([\\+\\-rwx\\s]+)\\s*,(.+)", Pattern.CASE_INSENSITIVE);
  private final PathsService myPathsService;

  public FileAccessParser(
    @NotNull final PathsService pathsService) {
    myPathsService = pathsService;
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

      for (String pathItem: antPatternsStr.split(",")) {
          final File path = new File(pathItem.trim());
          accessControlEntries.add(new AccessControlEntry(path, account, permissions));
        }
    }

    return new AccessControlList(accessControlEntries);
  }
}
