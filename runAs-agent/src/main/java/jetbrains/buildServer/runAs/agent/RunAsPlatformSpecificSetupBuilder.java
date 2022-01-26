/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
  private final Converter<String, String> myArgumentConverter;
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
    @NotNull final Converter<String, String> argumentConverter,
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
    myArgumentConverter = argumentConverter;
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
    for (AccessControlEntry ace: myAccessControlListProvider.getAcl(userCredentials)) {
      acl.add(ace);
    }

    acl.add(new AccessControlEntry(commandFile, AccessControlAccount.forUser(userCredentials.getUser()), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Step));

    final File runAsToolPath = getTool();
    final AccessControlEntry runAsToolAce = new AccessControlEntry(runAsToolPath, AccessControlAccount.forUser(userCredentials.getUser()), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Build);
    acl.add(runAsToolAce);

    myAccessControlResource.setAcl(new AccessControlList(acl));
    resources.add(myAccessControlResource);

    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup(
      runAsToolPath.getAbsolutePath(),
      Arrays.asList(
        new CommandLineArgument(settingsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(commandFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(myBuildAgentSystemInfo.bitness().toString(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(myArgumentConverter.convert(userCredentials.getPassword()), CommandLineArgument.Type.PARAMETER)),
      resources);

    myRunAsLogger.LogRunAs(userCredentials, commandLineSetup, runAsCommandLineSetup);
    return Collections.singleton(runAsCommandLineSetup);
  }

  private File getTool() {
    final File path = new File(myRunnerParametersService.getToolPath(Constants.RUN_AS_TOOL_NAME), TOOL_FILE_NAME + myCommandFileExtension);
    myFileService.validatePath(path);
    return path;
  }
}