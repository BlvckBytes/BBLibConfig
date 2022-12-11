package me.blvckbytes.bblibconfig.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiFunction;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/10/2022

  Lists all available number separation modes and their evaluation functions.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
@Getter
@AllArgsConstructor
public enum NumberSeparationMode {

  NONE((n, sep) -> n),
  TENS((n, sep)      -> separateNumber(n, sep, 1)),
  HUNDREDS((n, sep)  -> separateNumber(n, sep, 2)),
  THOUSANDS((n, sep) -> separateNumber(n, sep, 3)),
  MILLIONS((n, sep)  -> separateNumber(n, sep, 6)),
  ;

  // function(number_string, separator): result
  private final BiFunction<String, String, String> separation;

  private static String separateNumber(String numberString, String separator, int packSize) {
    StringBuilder result = new StringBuilder();

    // Start and end of the index range (inclusive) to be modified with separators
    int start = -1, end = 0;

    char[] numberChars = numberString.toCharArray();
    for (int i = 0; i < numberChars.length; i++) {
      char c = numberChars[i];

      if (start < 0) {

        // Not a significant digit, just padding
        if (!(c >= '1' && c <= '9'))
          continue;

        start = i;
      }

      // Hit something non-numerical, probably the comma separator, stop
      if (!(c >= '0' && c <= '9')) {
        end = i - 1;
        break;
      }
    }

    // Append padding (which is not modified), if applicable
    if (start > 0)
      result.append(numberString, 0, start);

    int firstPackSize = (end - start + 1) % packSize;

    if (firstPackSize == 0)
      firstPackSize = packSize;

    // Never append separators to the end, skip unnecessary iterations
    end -= packSize;

    int digitCounter = 0;
    boolean firstPack = true;

    for (int i = start; i <= end; i++) {
      result.append(numberChars[i]);

      if (++digitCounter == (firstPack ? firstPackSize : packSize)) {
        result.append(separator);
        digitCounter = 0;
        firstPack = false;
      }
    }

    // Apply decimal marker as well as decimal places (which are not modified), if applicable
    if (end != numberString.length() - 1)
      result.append(numberString.substring(end + 1));

    return result.toString();
  }
}
