package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.Nullable;

public interface SettingsProvider {
  @Nullable  Settings tryGetSettings();
}
