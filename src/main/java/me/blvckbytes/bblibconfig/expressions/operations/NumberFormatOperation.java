package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.AOperation;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.NumberFormatOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/11/2022

  Responds with the formatted number of it's input number.

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
public class NumberFormatOperation extends AOperation {

  public NumberFormatOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.NUMBER_FORMAT, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    NumberFormatOperationArgument args = (NumberFormatOperationArgument) expression.getArguments();

    // Evaluate inputs
    ConfigValue cvN = args.getNumber().evaluateAll(dataProvider);
    ConfigValue cvSep = args.getSeparationString().evaluateAll(dataProvider);
    ConfigValue cvDec = args.getDecimalString().evaluateAll(dataProvider);

    Optional<Double> number = tryParseNumber(flattenValue(cvN));

    // Not a valid number
    if (number.isEmpty())
      return cvN;

    String decimalMarker = cvDec.toString();

    // Apply rounding
    double rounded = args.getRoundingMode().getRound().apply(number.get(), args.getNumberOfDecimals());

    // Apply padding
    String result = stringifyNumber(rounded, args.getPaddingSize(), decimalMarker);

    // Apply separation
    result = args.getSeparationMode().getSeparation().apply(result, cvSep.toString());

    return ConfigValue.immediate(result);
  }

  /**
   * Stringifies a number by turning it into a string and applying
   * padding as well as a custom decimal notation
   * @param number Number to format
   * @param padding Min number of characters before the decimal or the end
   * @param decimal Preferred decimal notation character
   * @return Formatted string
   */
  private String stringifyNumber(double number, int padding, String decimal) {
    String result = String.valueOf(number);

    // Apply padding
    int deltaPadding = padding - result.indexOf('.');
    if (deltaPadding > 0)
      result = "0".repeat(deltaPadding) + result;

    // Substitute decimal notation
    result = result.replace(".", decimal);

    return result;
  }
}
