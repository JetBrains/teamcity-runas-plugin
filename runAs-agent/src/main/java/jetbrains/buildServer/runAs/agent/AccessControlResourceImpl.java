package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import org.jetbrains.annotations.NotNull;

public class AccessControlResourceImpl implements AccessControlResource {
  private final FileAccessService myFileAccessService;
  private AccessControlList myAccessControlList = new AccessControlList(Collections.<AccessControlEntry>emptyList());

  public AccessControlResourceImpl(@NotNull final FileAccessService fileAccessService) {
    myFileAccessService = fileAccessService;
  }

  @Override
  public void setAcl(@NotNull final AccessControlList accessControlList) {
    myAccessControlList = accessControlList;
  }

  @Override
  public void publishBeforeBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
    myFileAccessService.setAccess(myAccessControlList);
  }

  @Override
  public void publishAfterBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "AclResource",
      myAccessControlList);
  }
}
