package me.blvckbytes.bblibconfig.sections;

import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibutil.Tuple;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents an enchantment applied to an item stack.

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
public class ItemStackEnchantmentSection extends AConfigSection {

  private @Nullable ConfigValue enchantment;
  private @Nullable ConfigValue level;

  /**
   * Convert the properties of this section to an enchantment value as well as a level to apply
   * @return A Tuple on success, empty if enchantment was missing
   * @param variables Variables to apply while evaluating values
   */
  public @Nullable Tuple<Enchantment, Integer> asEnchantment(@Nullable Map<String, String> variables) {
    // Try to parse the enchantment, if provided
    Enchantment ench = (
      enchantment == null ?
        null :
        enchantment
          .copy()
          .withVariables(variables)
          .asScalar(Enchantment.class)
    );

    // No enchantment provided
    if (ench == null)
      return null;

    Integer lvl = (
      level == null ?
        null :
        level
          .copy()
          .withVariables(variables)
          .asScalar(Integer.class)
    );

    // Fall back to level 1 if not provided
    return new Tuple<>(ench, lvl == null ? 1 : lvl);
  }
}
