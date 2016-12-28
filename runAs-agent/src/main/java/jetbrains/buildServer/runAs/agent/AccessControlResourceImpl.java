package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessControlResourceImpl implements AccessControlResource {
  private final FileAccessService myFileAccessService;
  private final List<AccessControlEntry> aceList = new ArrayList<AccessControlEntry>();

  public AccessControlResourceImpl(@NotNull final FileAccessService fileAccessService) {
    myFileAccessService = fileAccessService;
  }

  @Override
  public void addEntry(@NotNull final AccessControlEntry accessControlEntry) {
    aceList.add(accessControlEntry);
  }

  @Override
  public void publishBeforeBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
    myFileAccessService.setAccess(new AccessControlList(aceList));
  }

  @Override
  public void publishAfterBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
  }
}
