package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.*;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;

public class RunAsPlatformSpecificSetupBuilder implements CommandLineSetupBuilder {
  static final String TOOL_FILE_NAME = "runAs";
  static final String ARGS_EXT = ".args";
  private final UserCredentialsService myUserCredentialsService;
  private final RunnerParametersService myRunnerParametersService;
  private final FileService myFileService;
  private final BuildAgentSystemInfo myBuildAgentSystemInfo;
  private final AccessControlListProvider myAccessControlListProvider;
  private final ResourcePublisher myBeforeBuildPublisher;
  private final AccessControlResource myAccessControlResource;
  private final ResourceGenerator<UserCredentials> myUserCredentialsGenerator;
  private final ResourceGenerator<RunAsParams> myRunAsCmdGenerator;
  private final FileAccessService myFileAccessService;
  private final RunAsLogger myRunAsLogger;
  private final RunAsAccessService myRunAsAccessService;
  private final String myCommandFileExtension;

  public RunAsPlatformSpecificSetupBuilder(
    @NotNull final UserCredentialsService userCredentialsService,
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final FileService fileService,
    @NotNull final BuildAgentSystemInfo buildAgentSystemInfo,
    @NotNull final AccessControlListProvider accessControlListProvider,
    @NotNull final ResourcePublisher beforeBuildPublisher,
    @NotNull final AccessControlResource accessControlResource,
    @NotNull final ResourceGenerator<UserCredentials> userCredentialsGenerator,
    @NotNull final ResourceGenerator<RunAsParams> runAsCmdGenerator,
    @NotNull final FileAccessService fileAccessService,
    @NotNull final RunAsLogger runAsLogger,
    @NotNull final RunAsAccessService runAsAccessService,
    @NotNull final String commandFileExtension) {
    myUserCredentialsService = userCredentialsService;
    myRunnerParametersService = runnerParametersService;
    myFileService = fileService;
    myBuildAgentSystemInfo = buildAgentSystemInfo;
    myAccessControlListProvider = accessControlListProvider;
    myBeforeBuildPublisher = beforeBuildPublisher;
    myAccessControlResource = accessControlResource;
    myUserCredentialsGenerator = userCredentialsGenerator;
    myRunAsCmdGenerator = runAsCmdGenerator;
    myFileAccessService = fileAccessService;
    myRunAsLogger = runAsLogger;
    myRunAsAccessService = runAsAccessService;
    myCommandFileExtension = commandFileExtension;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    // Get userCredentials
    final UserCredentials userCredentials = myUserCredentialsService.tryGetUserCredentials();
    if(userCredentials == null) {
      return Collections.singleton(commandLineSetup);
    }

    if(!myRunAsAccessService.getIsRunAsEnabled()) {
      throw new BuildStartException("RunAs is not enabled");
    }

    // Resources
    final ArrayList<CommandLineResource> resources = new ArrayList<CommandLineResource>();
    resources.addAll(commandLineSetup.getResources());

    // Settings
    final File settingsFile = myFileService.getTempFileName(ARGS_EXT);
    resources.add(new CommandLineFile(myBeforeBuildPublisher, settingsFile.getAbsoluteFile(), myUserCredentialsGenerator.create(userCredentials)));

    // Command
    List<CommandLineArgument> cmdLineArgs = new ArrayList<CommandLineArgument>();
    cmdLineArgs.add(new CommandLineArgument(commandLineSetup.getToolPath(), CommandLineArgument.Type.PARAMETER));
    cmdLineArgs.addAll(commandLineSetup.getArgs());

    final RunAsParams params = new RunAsParams(cmdLineArgs);

    final File commandFile = myFileService.getTempFileName(myCommandFileExtension);
    resources.add(new CommandLineFile(myBeforeBuildPublisher, commandFile.getAbsoluteFile(), myRunAsCmdGenerator.create(params)));
    final ArrayList<AccessControlEntry> acl = new ArrayList<AccessControlEntry>();
    acl.add(new AccessControlEntry(commandFile, AccessControlAccount.forUser(userCredentials.getUser()), EnumSet.of(AccessPermissions.GrantExecute)));
    final AccessControlList beforeBuildStepAcl = myAccessControlListProvider.getBeforeBuildStepAcl(userCredentials);
    for (AccessControlEntry ace: beforeBuildStepAcl) {
      acl.add(ace);
    }

    myAccessControlResource.setAcl(new AccessControlList(acl));
    resources.add(myAccessControlResource);

    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup(
      getTool().getAbsolutePath(),
      Arrays.asList(
        new CommandLineArgument(settingsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(commandFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(myBuildAgentSystemInfo.bitness().toString(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(userCredentials.getPassword(), CommandLineArgument.Type.PARAMETER)),
      resources);

    myRunAsLogger.LogRunAs(runAsCommandLineSetup);
    return Collections.singleton(runAsCommandLineSetup);
  }

  private File getTool() {
    final File path = new File(myRunnerParametersService.getToolPath(Constants.RUN_AS_TOOL_NAME), TOOL_FILE_NAME + myCommandFileExtension);
    myFileService.validatePath(path);
    final AccessControlList acl = new AccessControlList(Arrays.asList(new AccessControlEntry(path, AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantExecute))));
    myFileAccessService.setAccess(acl);
    return path;
  }
}