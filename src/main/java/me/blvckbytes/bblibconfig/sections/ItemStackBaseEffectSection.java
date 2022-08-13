package me.blvckbytes.bblibconfig.sections;

import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents the base effect set on a potion item stack.

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
public class ItemStackBaseEffectSection extends AConfigSection {

  private @Nullable ConfigValue type;
  private @Nullable Boolean extended;
  private @Nullable Boolean upgraded;

  /**
   * Convert the properties of this section to a PotionData object
   * @param variables Variables to apply while evaluating values
   */
  public PotionData asData(@Nullable Map<String, String> variables) {
    boolean _upgraded = upgraded != null && upgraded;
    boolean _extended = extended != null && extended;

    PotionType type = (
      this.type == null ?
        null :
        this.type
          .copy()
          .withVariables(variables)
          .asScalar(PotionType.class)
    );

    // Potions cannot be both extended and upgraded at the same
    // time, focus the priority on the upgraded flag
    return new PotionData(
      type == null ? PotionType.AWKWARD : type,
      !_upgraded && _extended, _upgraded
    );
  }

  /**
   * Compares all available values of this section against the
   * provided data and checks if they match
   * @param data Target data
   */
  public boolean describesData(PotionData data) {
    PotionType type = this.type == null ? null : this.type.asScalar(PotionType.class);
    if (type != null && type != data.getType())
      return false;

    if (extended != null && extended != data.isExtended())
      return false;

    if (upgraded != null && upgraded != data.isUpgraded())
      return false;

    // All checks passed
    return true;
  }
}
