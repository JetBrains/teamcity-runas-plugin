package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.Nullable;

public class ParameterUtils {
  public static boolean parseBoolean(@Nullable final String boolStr, final boolean defaultValue) {
    if(StringUtil.isEmptyOrSpaces(boolStr)) {
      return defaultValue;
    }

    if(Boolean.toString(true).equalsIgnoreCase(boolStr)) {
      return true;
    }

    if(Boolean.toString(false).equalsIgnoreCase(boolStr)) {
      return false;
    }

    return defaultValue;
  }
}
