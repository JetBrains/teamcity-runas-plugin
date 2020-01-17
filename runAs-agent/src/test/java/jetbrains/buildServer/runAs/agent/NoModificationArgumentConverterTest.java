/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class NoModificationArgumentConverterTest {

  @DataProvider(name = "convertCases")
  public Object[][] getConvertCases() {
    return new Object[][] {
      { "a b", "\"a b\"" },
      { "", "\"\"" },
      { " ", "\" \"" },
      { "a \"b", "\"a \"\"b\"" },
    };
  }

  @Test(dataProvider = "convertCases")
  public void shouldConvert(@NotNull final String arg, @NotNull final String expectedConvertedArf) {
    // Given
    final Converter<String, String> instance = createInstance();

    // When
    final String actualConvertedArf = instance.convert(arg);

    // Then
    then(actualConvertedArf ).isEqualTo(expectedConvertedArf);
  }

  @NotNull
  private Converter<String, String> createInstance()
  {
    return new WindowsArgumentConverter();
  }
}
