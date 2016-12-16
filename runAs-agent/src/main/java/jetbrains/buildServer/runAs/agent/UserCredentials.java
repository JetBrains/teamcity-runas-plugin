package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public class UserCredentials {
  private final String myUser;
  private final String myPassword;

  public UserCredentials(
    @NotNull final String user,
    @NotNull final String password) {
    myUser = user;
    myPassword = password;
  }

  @NotNull
  String getUser() {
    return myUser;
  }

  @NotNull
  String getPassword() {
    return myPassword;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final UserCredentials that = (UserCredentials)o;

    if (!myUser.equals(that.myUser)) return false;
    return myPassword.equals(that.myPassword);

  }

  @Override
  public int hashCode() {
    int result = myUser.hashCode();
    result = 31 * result + myPassword.hashCode();
    return result;
  }
}
