package me.blvckbytes.bblibconfig.expressions.operations;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.ILutResolver;
import me.blvckbytes.bblibconfig.expressions.AOperation;
import me.blvckbytes.bblibconfig.expressions.IExpressionDataProvider;
import me.blvckbytes.bblibconfig.expressions.IOperatorRegistry;
import me.blvckbytes.bblibconfig.expressions.ExpressionOperation;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibconfig.sections.operations.LutLookupOperationArgument;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Tries to lookup a key within a lookup table and offers missing key
  interception by providing a default value.

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
public class LutLookupOperation extends AOperation {

  public LutLookupOperation(
    @AutoInject IOperatorRegistry registry
  ) {
    registry.register(ExpressionOperation.LUT_LOOKUP, this);
  }

  @Override
  public ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider) {
    LutLookupOperationArgument args = (LutLookupOperationArgument) expression.getArguments();

    String lutName = args.getLutName().evaluateAll(dataProvider).toString();
    String lutKey = args.getLutKey().evaluateAll(dataProvider).toString();
    String defaultValue = args.getDefaultValue().evaluateAll(dataProvider).toString();

    ILutResolver lutResolver = dataProvider.getLutResolver();

    if (lutResolver == null)
      return ConfigValue.immediate(defaultValue);

    return ConfigValue.immediate(
      lutResolver.getLut(lutName)
      .map(lut -> lut.getOrDefault(lutKey, defaultValue))
      .orElse(defaultValue)
    );
  }
}
