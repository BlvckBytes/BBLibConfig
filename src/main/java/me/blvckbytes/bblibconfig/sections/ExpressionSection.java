package me.blvckbytes.bblibconfig.sections;

import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Represents an expression block which is to be evaluated at runtime
  in order to support dynamic content.

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
public class ExpressionSection extends AConfigSection {

  // An expression section always has to start with this marker string as it's root key
  public static final String MARKER = "$evaluate";

  // Operation to be performed
  @CSAlways
  private ExpressionOperation operation;

  // Arguments to this operation
  @CSAlways
  @CSInlined
  private Object arguments;

  @Override
  public Class<?> runtimeDecide(String field) {
    // Decide the arguments wrapper class based on the current operation type
    if (field.equals("arguments"))
      return operation.getArgumentSectionType();

    return super.runtimeDecide(field);
  }

  @Override
  public String toString() {
    return "ExpressionSection (\n" +
      "  operation=" + operation + "\n" +
      "  arguments=" + arguments + "\n" +
      ')';
  }
}
