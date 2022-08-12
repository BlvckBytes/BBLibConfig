package me.blvckbytes.bblibconfig.sections;

import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents a banner pattern set on a banner item stack.

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
public class ItemStackBannerPatternSection extends AConfigSection {

  private @Nullable ConfigValue pattern;
  private @Nullable ConfigValue color;

  /**
   * Convert the properties of this section to a Pattern object
   * @param variables Variables to apply while evaluating values
   */
  public @Nullable Pattern asPattern(@Nullable Map<String, String> variables) {
    PatternType pattern = this.pattern == null ? null : this.pattern.withVariables(variables).asScalar(PatternType.class);
    DyeColor color = this.color == null ? null : this.color.withVariables(variables).asScalar(DyeColor.class);

    // Cannot construct a pattern with missing data
    if (pattern == null || color == null)
      return null;

    return new Pattern(color, pattern);
  }
}
