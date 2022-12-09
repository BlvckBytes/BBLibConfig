package me.blvckbytes.bblibconfig.sections.operations;

import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Base class of an argument specific to an operation.

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
public abstract class AOperationArgument extends AConfigSection {

  /**
   * Decides how to stringify a config value based on it's internal items
   * @param value Value to stringify
   * @return Stringified value
   */
  protected String stringifyConfigValue(@Nullable ConfigValue value) {
    // No items available
    if (value == null)
      return "null";

    // No items available
    if (value.getItems().size() == 0)
      return "<empty>";

    // First item is an expression, stringify that
    Object first = value.getItems().get(0);
    if (first instanceof ExpressionSection)
      return first.toString();

    // Use the default toString implementation
    return value.toString();
  }
}
