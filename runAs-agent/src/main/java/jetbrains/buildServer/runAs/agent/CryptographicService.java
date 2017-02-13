package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.Nullable;

public interface CryptographicService {
  @Nullable
  String unscramble(@Nullable String encryptedString);
}
