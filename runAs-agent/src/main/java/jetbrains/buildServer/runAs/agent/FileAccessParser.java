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

import com.intellij.openapi.diagnostic.Logger;
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
  private static final Logger LOG = Logger.getInstance(FileAccessParser.class.getName());
  private static final Pattern OutAccessPattern = Pattern.compile("\\s*([gbsrcua\\s]+)\\s*([\\+\\-rwx\\s]+)\\s*,(.+)", Pattern.CASE_INSENSITIVE);

  @NotNull
  @Override
  public AccessControlList parse(@NotNull final String aclString) {
    final ArrayList<AccessControlEntry> accessControlEntries = new ArrayList<AccessControlEntry>();
    if(StringUtil.isEmptyOrSpaces(aclString)) {
      return new AccessControlList(accessControlEntries);
    }

    final String[] entries = aclString.split(";");
    for (String aclEntryStr: entries) {
      final Matcher aclMatch = OutAccessPattern.matcher(aclEntryStr);
      if(!aclMatch.find() || aclMatch.groupCount() != 3) {
        throw new BuildStartException(String.format("Invalid ACL specification \"%s\"", aclEntryStr));
      }

      final String targetStr = aclMatch.group(1).toLowerCase().trim();
      final String permissionsStr = aclMatch.group(2).toLowerCase().trim();
      final String antPatternsStr = aclMatch.group(3).trim();

      final EnumSet<AccessPermissions> permissions = EnumSet.noneOf(AccessPermissions.class);
      AccessControlScope scope = AccessControlScope.Step;
      AccessControlAccount account = null;
      for(char targetChar: targetStr.toCharArray()) {
        switch (targetChar) {
          case ' ':
            break;

          case 'g':
            scope = AccessControlScope.Global;
            break;

          case 'b':
            scope = AccessControlScope.Build;
            break;

          case 's':
            scope = AccessControlScope.Step;
            break;

          case 'r':
            permissions.add(AccessPermissions.Recursive);
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

      boolean allow = true;
      for(char permissionChar: permissionsStr.toCharArray()) {
        switch (permissionChar) {
          case ' ':
          case '+':
            allow = true;
            break;

          case '-':
            allow = false;
            break;

          case 'r':
            if(allow) {
              permissions.add(AccessPermissions.GrantRead);
            }
            else {
              permissions.add(AccessPermissions.DenyRead);
            }
            break;

          case 'w':
            if(allow) {
              permissions.add(AccessPermissions.GrantWrite);
            }
            else {
              permissions.add(AccessPermissions.DenyWrite);
            }
            break;

          case 'x':
            if(allow) {
              permissions.add(AccessPermissions.GrantExecute);
            }
            else {
              permissions.add(AccessPermissions.DenyExecute);
            }
            break;

          default:
            throw new BuildStartException(String.format("Invalid ACL specification \"%s\"", aclEntryStr));
        }
      }

      for (String pathItem: antPatternsStr.split(",")) {
          final File path = new File(pathItem.trim());
          accessControlEntries.add(new AccessControlEntry(path, account, permissions, scope));
        }
    }

    final AccessControlList acl = new AccessControlList(accessControlEntries);
    if(LOG.isDebugEnabled()) {
      LOG.debug("parse: \"" + aclString + "\" as " + acl);
    }

    return acl;
  }
}
