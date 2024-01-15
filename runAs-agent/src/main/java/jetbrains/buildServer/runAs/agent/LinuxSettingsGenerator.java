

package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class LinuxSettingsGenerator implements ResourceGenerator<UserCredentials> {
  @NotNull
  @Override
  public String create(@NotNull final UserCredentials userCredentials) {
    final StringBuilder sb = new StringBuilder();

    final String user = userCredentials.getUser();
    if(!StringUtil.isEmptyOrSpaces(user)) {
      sb.append(userCredentials.getUser());
    }
    
    return sb.toString();
  }
}