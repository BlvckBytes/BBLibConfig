package me.blvckbytes.bblibconfig.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Lists all available text formatting flags usable on text.

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
public enum TextFormatting {

  BOLD('l'),
  ITALIC('o'),
  UNDERLINED('n'),
  STRIKETHROUGH('m'),
  OBFUSCATED('k'),
  ;

  private final char marker;

  public static TextFormatting[] values = values();

  // Mapping marking characters to enum constants for quick access
  private static final Map<Character, TextFormatting> lut;

  static {
    // Initialize the lookup table on all available values
    lut = new HashMap<>();
    for (TextFormatting fmt : values)
      lut.put(fmt.getMarker(), fmt);
  }

  /**
   * Find a text formatting constant by it's representitive marking character
   * @param c Marking character to search for
   * @return Text formatting constant or null if the char is unknown
   */
  public static @Nullable TextFormatting getByChar(char c) {
    return lut.get(c);
  }
}
