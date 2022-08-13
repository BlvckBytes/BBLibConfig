package me.blvckbytes.bblibconfig;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class ExpressionEvaluator implements IExpressionEvaluator {

  // TODO: Heavily clean the following mess up...

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  @Override
  public String evaluate(String input, IExpressionDataProvider dataProvider) {

    // Apply colors on the base string
    if (dataProvider.areColorsEnabled())
      input = applyColors(input);

    StringBuilder sb = new StringBuilder();
    List<Character> quoteStack = new ArrayList<>();

    int startIndInp = -1, startIndSb = -1;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      sb.append(c);

      if (i == input.length() - 1)
        break;

      // Unescaped string start/end marker within a variable expression
      boolean isEscaped = i != 0 && input.charAt(i - 1) == '\\';
      if ((c == '\'' || c == '"') && !isEscaped && startIndInp >= 0) {
        // Was a closing quote, remove
        if (quoteStack.size() > 0 && quoteStack.get(quoteStack.size() - 1).equals(c))
          quoteStack.remove(quoteStack.size() - 1);
          // Opened a new quote, add
        else
          quoteStack.add(c);
      }

      char n = input.charAt(i + 1);

      // Store the possible variable begin marker
      if (c == '{' && n == '{' && quoteStack.size() == 0) {
        startIndInp = i;
        startIndSb = sb.length() - 1;
        continue;
      }

      // Substitute the variable value
      if (c == '}' && n == '}' && quoteStack.size() == 0 && startIndInp >= 0) {
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
  public boolean parseBoolean(String value) {
    value = value.toLowerCase().trim();
    return value.equals("1") || value.equals("true");
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
    return ChatColor.translateAlternateColorCodes('&', input);
  }

  private @Nullable String resolveVariable(String expr, IExpressionDataProvider dataProvider) {
    expr = expr.trim();

    // Immediate string value, also support for inner variables and escaped quotes
    if (
      (expr.startsWith("\"") && expr.endsWith("\"")) ||
        (expr.startsWith("'") && expr.endsWith("'"))
    )
      return evaluate(removeQuotation(expr), dataProvider);

    // Just a numeric constant, return as is
    if (expr.matches("\\d+"))
      return expr;

    int indexQ = realIndexOf(expr, '?'), indexC = realIndexOf(expr, ':');
    int indexS = realIndexOf(expr, '['), indexE = realIndexOf(expr, ']');

    if (
      // Is a boolean expression
      indexQ >= 0 && indexC >= 0 &&
        // And the boolean expression comes first
        (indexQ < indexS || indexS < 0)
    ) {
      String v = expr.substring(0, indexQ);
      boolean bool;

      // Is a comparison
      int indexEq = realIndexOf(v, '=');
      if (indexEq >= 0) {

        String valueV = resolveVariable(v.substring(indexEq + 1).trim(), dataProvider);
        String nameV = resolveVariable(v.substring(0, indexEq), dataProvider);

        // Value(s) not found
        if (nameV == null || valueV == null)
          return null;

        bool = valueV.equals(nameV);
      }

      // The variable value is interpreted as a boolean
      else {
        boolean invert = v.startsWith("!");

        // Inverted, strip notation from variable name
        if (invert)
          v = v.substring(1);

        String vV = resolveVariable(v, dataProvider);

        // Name not found
        if (vV == null)
          return null;

        bool = parseBoolean(vV) ^ invert;
      }

      // Cut true and false cases
      String vT = expr.substring(indexQ + 1, indexC);
      String vF = expr.substring(indexC + 1);

      return bool ? resolveVariable(vT, dataProvider) : resolveVariable(vF, dataProvider);
    }

    if (
      // Is a lookup table expression
      indexS >= 0 && indexE >= 0 && dataProvider.getLutResolver() != null &&
        // And the boolean expression comes first
        (indexS < indexQ || indexQ < 0)
    ) {
      String lutName = expr.substring(0, indexS);
      String keyName = resolveVariable(expr.substring(indexS + 1, indexE), dataProvider);

      // LUT not found
      Map<String, String> lut = dataProvider.getLutResolver().getLut(lutName).orElse(null);
      if (lut == null || keyName == null)
        return null;

      // Key not found
      String res = lut.get(keyName);
      if (res == null)
        return null;

      return dataProvider.areColorsEnabled() ? applyColors(res) : res;
    }

    // Is a loop mapper expression
    // Syntax: {{ <var> %<sep> "<iter value>" }}
    int indexP = realIndexOf(expr, '%');
    if (indexP > 0 && indexP < expr.length() - 1) {
      String varVal = dataProvider.getVariables().get(expr.substring(0, indexP).trim());

      // Could not find requested variable
      if (varVal == null)
        return null;

      char sep = expr.charAt(indexP + 1);
      String iterValue = removeQuotation(expr.substring(indexP + 2).trim());

      // Split on the provided separator
      String[] values = varVal.split(String.valueOf(sep));

      // No values provided, return empty string
      if (values.length == 1 && values[0].isBlank())
        return "";

      // Iterate all values after the split and join them
      StringBuilder res = new StringBuilder();

      // Create a local variable map to force-shadow "it" and "ind"
      Map<String, String> localVars = new HashMap<>(dataProvider.getVariables());

      IExpressionDataProvider internalDataProvider = new IExpressionDataProvider() {

        @Override
        public @Nullable ILutResolver getLutResolver() {
          return dataProvider.getLutResolver();
        }

        @Override
        public Map<String, String> getVariables() {
          return localVars;
        }

        @Override
        public DateFormat getSerializationFormat() {
          return dataProvider.getSerializationFormat();
        }

        @Override
        public boolean areColorsEnabled() {
          return dataProvider.areColorsEnabled();
        }
      };

      for (int i = 0; i < values.length; i++) {
        localVars.put("it", values[i].trim());
        localVars.put("ind", String.valueOf(i));

        res.append(evaluate(iterValue, internalDataProvider));
      }

      return dataProvider.areColorsEnabled() ? applyColors(res.toString()) : res.toString();
    }

    // Is a date format expression
    // Syntax: {{ <var> $ "<date format>" }}
    int indexD = realIndexOf(expr, '$');
    if (indexD > 0 && indexD < expr.length() - 1) {
      String varVal = dataProvider.getVariables().get(expr.substring(0, indexD).trim());

      // Could not find requested variable
      if (varVal == null)
        return null;

      try {
        // Try to parse the date format
        return new SimpleDateFormat(removeQuotation(expr.substring(indexD + 1).trim()))
          // Try to parse the serialized date and then format using the specified format
          .format(dataProvider.getSerializationFormat().parse(varVal));
      }

      // Either an invalid format or not a date containing variable
      catch (Exception e) {
        return null;
      }
    }

    // Just try to lookup as is
    String res = dataProvider.getVariables().get(expr);
    return res == null ? null : (dataProvider.areColorsEnabled() ? applyColors(res) : res);
  }

  /**
   * Removes outer quotation of any value and un-escapes inner quotes
   */
  private String removeQuotation(String input) {
    // Immediate string value, also support for inner variables and escaped quotes
    if (
      (input.startsWith("\"") && input.endsWith("\"")) ||
        (input.startsWith("'") && input.endsWith("'"))
    ) {
      return input.substring(1, input.length() - 1)
        .replace("\\\"", "\"")
        .replace("\\'", "'");
    }

    return input;
  }

  /**
   * Searches for the first real occurrence of a character
   * within a string, where real is defined to be a position
   * outside of any string, marked by "" or ''.
   * @param str String to search in
   * @param c Character to search for
   * @return Position, -1 if the char does not exist
   */
  private int realIndexOf(String str, char c) {

    List<Character> quoteStack = new ArrayList<>();
    char[] chars = str.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      boolean isEscaped = i != 0 && chars[i - 1] == '\\';
      char sC = chars[i];

      // Unescaped string start/end marker
      if ((sC == '\'' || sC == '"') && !isEscaped) {
        // Was a closing quote, remove
        if (quoteStack.size() > 0 && quoteStack.get(quoteStack.size() - 1).equals(sC))
          quoteStack.remove(quoteStack.size() - 1);
          // Opened a new quote, add
        else
          quoteStack.add(sC);
      }

      // Currently within a quote or not matching the target
      if (quoteStack.size() > 0 || sC != c)
        continue;

      return i;
    }

    // Target not found
    return -1;
  }
}
