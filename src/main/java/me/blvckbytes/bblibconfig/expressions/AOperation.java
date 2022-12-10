package me.blvckbytes.bblibconfig.expressions;

import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.sections.ExpressionSection;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Base class of all operators which hoists up common routines.

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
public abstract class AOperation {

  // 0.0, .0, 0
  private static final Pattern FLOAT_PATTERN = Pattern.compile("^\\d*\\.?\\d+$");
  private static final DateFormat DATE_ISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  /**
   * Called whenever this operation is requested to be performed
   * @param expression Expression to be performed
   * @param dataProvider Data provider for the expression's context
   * @return Resulting path after executing the operation on the input value(s)
   */
  public abstract ConfigValue execute(ExpressionSection expression, IExpressionDataProvider dataProvider);

  /**
   * Flattens a config value into a single object value which can be used for comparison by
   * either using it as a scalar value if it already is one or by scalarizing it
   * @param value Value to flatten
   * @return Flattened value
   */
  protected Object flattenValue(ConfigValue value) {
    // Already a scalar value (no string, thus no variables to substitute)
    if (value.getItems().size() == 1 && !(value.getItems().get(0) instanceof String))
      return value.getItems().get(0);

    // Flatten by invoking it's inner scalarization routine
    return value.toString();
  }

  /**
   * Tries to parse a number (internally always a double) from an object by
   * either converting it or parsing the number from a string representation
   * @param input Input value
   * @return Parsed double, available if possible
   */
  protected Optional<Double> tryParseNumber(Object input) {
    // Already a double
    if (input instanceof Double)
      return Optional.of((Double) input);

    // Convert floats to doubles
    if (input instanceof Float)
      return Optional.of(Double.valueOf((Float) input));

    if (input instanceof Long)
      return Optional.of(Double.valueOf((Long) input));

    if (input instanceof Integer)
      return Optional.of(Double.valueOf((Integer) input));

    // Try to parse the number from a string
    if (input instanceof String) {
      if (FLOAT_PATTERN.matcher((String) input).matches())
        return Optional.of(Double.parseDouble((String) input));
    }

    // Not a number
    return Optional.empty();
  }

  /**
   * Tries to parse a date from an object by either casting it or parsing
   * the date from an ISO string representation or a unix timestamp number
   * @param input Input value
   * @return Parsed date, available if possible
   */
  protected Optional<Date> tryParseDate(Object input) {
    // Already a date
    if (input instanceof Date)
      return Optional.of((Date) input);

    // Try to parse the date from a unix timestamp number
    Optional<Double> number = tryParseNumber(input);
    if (number.isPresent())
      return number.map(num -> new Date(num.longValue()));

    // Try to parse the date from an ISO string
    if (input instanceof String) {
      try {
        return Optional.of(DATE_ISO.parse((String) input));
      } catch (Exception ignored) {}
    }

    // Not a date
    return Optional.empty();
  }

  /**
   * Decides whether a config value is truthy ("true", "yes", "1", > 0)
   * @param cv Value in question
   * @return True if truthy, false in all other cases
   */
  protected boolean isTruthy(ConfigValue cv) {
    Object value = flattenValue(cv);
    Optional<Double> number = tryParseNumber(value);

    return number
      .map(v -> v > 0)
      .orElseGet(() -> {
        String str = value.toString();
        return (
          str.equalsIgnoreCase("true") ||
          str.equalsIgnoreCase("yes") ||
          str.equalsIgnoreCase("1")
        );
      });
  }

  /**
   * Returns the evaluated result if provided or creates an immediate config value based on the fallback scalar
   * @param result Optional result value
   * @param dataProvider Data provider to evaluate the result before responding
   * @param fallback Fallback value if the result is null
   * @return Guaranteed non-null and evaluated result value
   */
  protected ConfigValue resultOrFallback(@Nullable ConfigValue result, IExpressionDataProvider dataProvider, Object fallback) {
    return result == null ? ConfigValue.immediate(fallback) : result.evaluateAll(dataProvider);
  }
}
