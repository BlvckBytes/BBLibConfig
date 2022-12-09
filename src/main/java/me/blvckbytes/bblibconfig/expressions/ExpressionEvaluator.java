package me.blvckbytes.bblibconfig.expressions;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Evaluator implementation used to evaluate configuration expressions.

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
public class ExpressionEvaluator implements IExpressionEvaluator, IOperatorRegistry {

  // Mapping operators to their executors
  private final Map<ExpressionOperation, AOperation> operators;

  public ExpressionEvaluator(
    @AutoInject ILogger logger
  ) {
    this.operators = new HashMap<>();
  }

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  @Override
  public String substituteVariables(String input, IExpressionDataProvider dataProvider) {

    // Apply colors on the base string
    if (dataProvider.areColorsEnabled())
      input = applyColors(input);

    StringBuilder sb = new StringBuilder();

    int startIndInp = -1, startIndSb = -1;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      sb.append(c);

      if (i == input.length() - 1)
        break;

      char n = input.charAt(i + 1);

      // Store the possible variable begin marker
      if (c == '{' && n == '{') {
        startIndInp = i;
        startIndSb = sb.length() - 1;
        continue;
      }

      // Substitute the variable value
      if (c == '}' && n == '}' && startIndInp >= 0) {
        String name = input.substring(startIndInp + 2, i);
        String value = resolveVariable(name, dataProvider);

        // Variable found, substitute
        if (value != null) {
          sb.delete(startIndSb, sb.length());

          // Find the last color specified
          String color = ChatColor.getLastColors(sb.toString());

          // Apply affecting colors on all lines of the variable
          sb.append(
            Arrays.stream(value.split("\n"))
              .collect(Collectors.joining("\n" + color, color, ""))
          );

          // Skip the second closing bracket
          i++;
        }

        // End of input reached
        if (i == input.length() - 1)
          break;

        startIndSb = startIndInp = -1;
      }
    }

    return sb.toString();
  }

  @Override
  public ConfigValue evaluateExpression(ExpressionSection input, IExpressionDataProvider dataProvider) {
    if (!operators.containsKey(input.getOperation()))
      return ConfigValue.immediate("unsupported operation (" + input.getOperation() + ")");

    return operators.get(input.getOperation()).execute(input, dataProvider);
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  /**
   * Translates color notations on the input string
   * @param input String to translate in
   * @return Translated string
   */
  private String applyColors(String input) {
    // Translate either on vanilla color code sequences, hex notations or on gradient notations
    return input.replaceAll(
      "&([\\da-zklmnor#]|<[#A-Za-z\\d :.]+>)", "§$1"
    );
  }

  private @Nullable String resolveVariable(String expr, IExpressionDataProvider dataProvider) {
    expr = expr.trim();

    // Immediate string value, also support for inner variables and escaped quotes
    if (
      (expr.startsWith("\"") && expr.endsWith("\"")) ||
      (expr.startsWith("'") && expr.endsWith("'"))
    )
      return expr.substring(1, expr.length() - 1);

    // Just a numeric constant, return as is
    if (expr.matches("\\d+"))
      return expr;

    int indexS = expr.indexOf('['), indexE = expr.indexOf(']');

    // Is a lookup table expression
    if (indexS >= 0 && indexE >= 0 && dataProvider.getLutResolver() != null) {
      String lutName = expr.substring(0, indexS);
      String keyName = resolveVariable(expr.substring(indexS + 1, indexE), dataProvider);

      // LUT not found or key unresolvable
      Map<String, String> lut = dataProvider.getLutResolver().getLut(lutName).orElse(null);
      if (lut == null || keyName == null)
        return null;

      // Key not found
      String res = lut.get(keyName);
      if (res == null)
        return null;

      return dataProvider.areColorsEnabled() ? applyColors(res) : res;
    }

    // Just try to lookup as is
    String res = dataProvider.getVariables().get(expr);
    return res == null ? null : (dataProvider.areColorsEnabled() ? applyColors(res) : res);
  }

  @Override
  public void register(ExpressionOperation type, AOperation operation) {
    this.operators.put(type, operation);
  }
}
