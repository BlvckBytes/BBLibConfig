package me.blvckbytes.bblibconfig.sections.operations;

import lombok.Getter;
import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.sections.CSAlways;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

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
@CSAlways
public class LutLookupOperationArgument extends AOperationArgument {

  private ConfigValue lutName;
  private ConfigValue lutKey;
  private ConfigValue defaultValue;

  @Override
  public String toString() {
    return "LutLookupOperationArgument (" + "\n" +
      "  lutName=" + lutName + "\n" +
      "  lutKey=" + lutKey + "\n" +
      "  defaultValue=" + defaultValue + "\n" +
      ')';
  }
}
