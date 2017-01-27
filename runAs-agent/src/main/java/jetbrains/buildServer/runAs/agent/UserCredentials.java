package jetbrains.buildServer.runAs.agent;

import java.util.HashMap;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;

public class UserCredentials {
  private final String myUser;
  private final String myPassword;
  private final WindowsIntegrityLevel myWindowsIntegrityLevel;
  private final LoggingLevel myLoggingLevel;
  private final List<CommandLineArgument> myAdditionalArgs;
  private final AccessControlList myBeforeStepAcl;

  public UserCredentials(
    @NotNull final String user,
    @NotNull final String password,
    @NotNull final WindowsIntegrityLevel windowsIntegrityLevel,
    @NotNull final LoggingLevel loggingLevel,
    @NotNull final List<CommandLineArgument> additionalArgs,
    @NotNull final AccessControlList beforeStepAcl) {
    myUser = user;
    myPassword = password;
    myWindowsIntegrityLevel = windowsIntegrityLevel;
    myLoggingLevel = loggingLevel;
    myAdditionalArgs = additionalArgs;
    myBeforeStepAcl = beforeStepAcl;
  }

  @NotNull
  String getUser() {
    return myUser;
  }

  @NotNull
  String getPassword() {
    return myPassword;
  }

  public WindowsIntegrityLevel getWindowsIntegrityLevel() {
    return myWindowsIntegrityLevel;
  }

  public LoggingLevel getLoggingLevel() {
    return myLoggingLevel;
  }

  @NotNull
  List<CommandLineArgument> getAdditionalArgs() {
    return myAdditionalArgs;
  }

  @NotNull
  public AccessControlList getBeforeStepAcl() {
    return myBeforeStepAcl;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final UserCredentials that = (UserCredentials)o;

    if (!myUser.equals(that.myUser)) return false;
    if (!myPassword.equals(that.myPassword)) return false;
    if (myWindowsIntegrityLevel != that.myWindowsIntegrityLevel) return false;
    if (myLoggingLevel != that.myLoggingLevel) return false;
    if (!myAdditionalArgs.equals(that.myAdditionalArgs)) return false;
    return myBeforeStepAcl.equals(that.myBeforeStepAcl);

  }

  @Override
  public int hashCode() {
    int result = myUser.hashCode();
    result = 31 * result + myPassword.hashCode();
    result = 31 * result + myWindowsIntegrityLevel.hashCode();
    result = 31 * result + myLoggingLevel.hashCode();
    result = 31 * result + myAdditionalArgs.hashCode();
    result = 31 * result + myBeforeStepAcl.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "UserCredentials",
      new HashMap<String, Object>() {{
        this.put("User", myUser);
        this.put("WindowsIntegrityLevel", myWindowsIntegrityLevel);
        this.put("LoggingLevel", myLoggingLevel);
        this.put("AdditionalArgs", LogUtils.toString(myAdditionalArgs));
        this.put("User", myUser);
        this.put("BeforeStepAcl", LogUtils.toString(myBeforeStepAcl));
      }});
  }
}
