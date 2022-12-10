package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.expressions.AOperation;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.DateFormatOperationArgument;
import me.blvckbytes.bblibconfig.sections.operations.MathOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/10/2022

  Responds with the formatted string version of it's input date.

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
public class DateFormatOperation extends AOperation {

  private final Map<String, DateFormat> formats;

  public DateFormatOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.DATE_FORMAT, this);
    this.formats = new HashMap<>();
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    DateFormatOperationArgument args = (DateFormatOperationArgument) expression.getArguments();

    // Evaluate inputs
    ConfigValue cvD = args.getDate().evaluateAll(dataProvider);
    String format = args.getFormat().evaluateAll(dataProvider).toString().trim();

    Optional<Date> date = tryParseDate(flattenValue(cvD));

    // Not a valid date
    if (date.isEmpty())
      return cvD;

    // Create the date format once if not yet cached
    if (!formats.containsKey(format))
      formats.put(format, new SimpleDateFormat(format));

    return ConfigValue.immediate(formats.get(format).format(date.get()));
  }
}
