package me.blvckbytes.bblibconfig.expressions;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Represents an operation implementation.

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
public interface IOperation {

  /**
   * Called whenever this operation is requested to be performed
   * @param expression Expression to be performed
   * @param dataProvider Data provider for the expression's context
   * @return Resulting path after executing the operation on the input value(s)
   */
  ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider);

}
