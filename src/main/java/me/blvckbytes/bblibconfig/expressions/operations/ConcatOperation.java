package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.AOperation;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.ConcatOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/10/2022

  Concatenates it's inputs while interpreting them as strings.

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
public class ConcatOperation extends AOperation {

  public ConcatOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.CONCAT, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    ConcatOperationArgument args = (ConcatOperationArgument) expression.getArguments();

    String stringA = args.getStringA().evaluateAll(dataProvider).toString();
    String stringB = args.getStringB().evaluateAll(dataProvider).toString();

    StringBuilder result = new StringBuilder(stringA);

    if (args.getSeparator() != null)
      result.append(args.getSeparator().evaluateAll(dataProvider).toString());

    result.append(stringB);

    return ConfigValue.immediate(result.toString());
  }
}
