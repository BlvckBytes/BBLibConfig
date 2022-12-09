package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperation;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.EqualsStrictOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Responds with true if both values match exactly and yields false otherwise.

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
public class EqualsStrictOperation implements IOperation {

  public EqualsStrictOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.EQUALS_STRICT, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    EqualsStrictOperationArgument args = (EqualsStrictOperationArgument) expression.getArguments();
    boolean result = args.getValueA().evaluateAll(dataProvider).toString().equals(args.getValueB().evaluateAll(dataProvider).toString());

    return ConfigValue.immediate(
      result ? (
        args.getPositive() == null ? true : args.getPositive().evaluateAll(dataProvider)
      ) : (
        args.getNegative() == null ? false : args.getNegative().evaluateAll(dataProvider)
      )
    );
  }
}
