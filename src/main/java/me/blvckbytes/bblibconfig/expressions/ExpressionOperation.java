package me.blvckbytes.bblibconfig.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.sections.operations.*;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Lists all available expression operations as well as their custom argument wrappers.

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
public enum ExpressionOperation {

  IF(IfOperationArgument.class),
  EQUALS(EqualsOperationArgument.class),
  LUT_LOOKUP(LutLookupOperationArgument.class),
  OR(OrOperationArgument.class),
  COMPARE(CompareOperationArgument.class),
  CONCAT(ConcatOperationArgument.class),
  MATH(MathOperationArgument.class),
  DATE_FORMAT(DateFormatOperationArgument.class),
  ;

  private final Class<? extends AConfigSection> argumentSectionType;

}
