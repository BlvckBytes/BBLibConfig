package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.AOperation;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.IfOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

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
public class IfOperation extends AOperation {

  public IfOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.IF, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    IfOperationArgument args = (IfOperationArgument) expression.getArguments();
    ConfigValue value = args.getBool().evaluateAll(dataProvider);
    return isTruthy(value) ? resultOrFallback(args.getPositive(), dataProvider, true) : resultOrFallback(args.getNegative(), dataProvider, false);
  }
}
