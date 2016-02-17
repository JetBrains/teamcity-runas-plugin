package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public class EnvironmentVariable {
  private final String myName;

  public EnvironmentVariable(@NotNull final String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final EnvironmentVariable that = (EnvironmentVariable)o;

    return getName().equals(that.getName());

  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
