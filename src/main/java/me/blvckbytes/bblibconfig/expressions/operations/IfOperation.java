package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperation;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.IfOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

import java.util.regex.Pattern;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Responds positively if it's primary input is truthy.

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
@AutoConstruct
public class IfOperation implements IOperation {

  // 0.0, .0, 0
  private static final Pattern FLOAT_PATTERN = Pattern.compile("^\\d*\\.?\\d+$");

  public IfOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.IF, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    IfOperationArgument args = (IfOperationArgument) expression.getArguments();
    String value = args.getBool().evaluateAll(dataProvider).toString().trim();

    boolean result = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");

    // Not yet valid, test for numbers
    if (!result && FLOAT_PATTERN.matcher(value).matches())
      result = Float.parseFloat(value) > 0;

    return result ? args.getPositive().evaluateAll(dataProvider) : args.getNegative().evaluateAll(dataProvider);
  }
}
