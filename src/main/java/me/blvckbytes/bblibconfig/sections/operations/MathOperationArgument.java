package me.blvckbytes.bblibconfig.sections.operations;

import lombok.Getter;
import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.MathMode;
import me.blvckbytes.bblibconfig.sections.CSAlways;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/10/2022

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
public class MathOperationArgument extends AOperationArgument {

  @CSAlways
  private ConfigValue valueA;

  @CSAlways
  private ConfigValue valueB;

  @CSAlways
  private MathMode mode;

  @Override
  public String toString() {
    return "MathOperationArgument (" + "\n" +
      "  valueA=" + valueA + "\n" +
      "  valueB=" + valueB + "\n" +
      "  mode=" + mode + "\n" +
      ')';
  }
}