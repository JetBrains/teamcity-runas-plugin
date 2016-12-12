package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessControlResourceImpl implements AccessControlResource {
  private final FileAccessService myFileAccessService;
  @Nullable private AccessControlList myAccessControlList;

  public AccessControlResourceImpl(@NotNull final FileAccessService fileAccessService) {
    myFileAccessService = fileAccessService;
  }

  public void setAccess(@NotNull final AccessControlList accessControlList) {
    myAccessControlList = accessControlList;
  }

  @Override
  public void publishBeforeBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
    if(myAccessControlList == null) {
      return;
    }

    myFileAccessService.setAccess(myAccessControlList);
  }

  @Override
  public void publishAfterBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
  }
}
