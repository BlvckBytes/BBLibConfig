package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.AOperation;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.CompareOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Responds with true if A compares positively against B.

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
public class CompareOperation extends AOperation {

  public CompareOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.COMPARE, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    CompareOperationArgument args = (CompareOperationArgument) expression.getArguments();

    // Evaluate both inputs
    ConfigValue cvA = args.getValueA().evaluateAll(dataProvider);
    ConfigValue cvB = args.getValueB().evaluateAll(dataProvider);

    // Check if they're both numbers
    Optional<Double> numberA = tryParseNumber(flattenValue(cvA));
    Optional<Double> numberB = tryParseNumber(flattenValue(cvB));

    // If so, compare numbers to avoid possible mismatches on different string formatting styles
    if (numberA.isPresent() && numberB.isPresent()) {
      return (
        args.getMode().getCompare().apply(numberA.get(), numberB.get()) ?
          resultOrFallback(args.getPositive(), dataProvider, true) :
          resultOrFallback(args.getNegative(), dataProvider, false)
      );
    }

    // TODO: Implement date comparison

    // Non-comparable types, always result in false
    return resultOrFallback(args.getNegative(), dataProvider, false);
  }
}
