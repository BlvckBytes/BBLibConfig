package me.blvckbytes.bblibconfig.expressions;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Represents the evaluator used to evaluate configuration expressions.

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
public interface IExpressionEvaluator {

  /**
   * Evaluate a string's result which may contain evaluateable expressions
   * @param input Input string
   * @param dataProvider Data provider which makes up the evaluation context
   * @return Evaluated result
   */
  String substituteVariables(String input, IExpressionDataProvider dataProvider);

  /**
   * Evaluate an expression section's result which may contain nested expressions
   * @param input Input expression
   * @param dataProvider Data provider which makes up the evaluation context
   * @return Evaluated result
   */
  ConfigValue evaluateExpression(ExpressionSection input, IExpressionDataProvider dataProvider);

}
