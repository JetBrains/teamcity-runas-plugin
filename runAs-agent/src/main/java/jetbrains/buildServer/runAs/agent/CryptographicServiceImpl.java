package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.Nullable;

public class CryptographicServiceImpl implements CryptographicService {
  @Nullable
  public String unscramble(@Nullable String str){
    if(StringUtil.isEmptyOrSpaces(str)) {
      return str;
    }

    if(!EncryptUtil.isScrambled(str))
    {
      return str;
    }

    return EncryptUtil.unscramble(str);
  }
}