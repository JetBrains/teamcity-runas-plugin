

package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.SystemInfo;

public class EnvironmentImpl implements Environment {
  @Override
  public OperationSystem getOperationSystem() {
    if(SystemInfo.isWindows) {
      return OperationSystem.Windows;
    }

    if(SystemInfo.isMac) {
      return OperationSystem.Mac;
    }

    return OperationSystem.Other;
  }
}