package me.blvckbytes.bblibconfig;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.blvckbytes.bblibutil.component.GradientGenerator;
import me.blvckbytes.bblibutil.component.TextComponent;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/25/2022

  Represents a configuration value and offers multiple
  parsing and conversion features.

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigValue implements IExpressionDataProvider {

  ///////////////////////////////// Constants /////////////////////////////////

  // Decimal format used when encountering double variables
  private static final DecimalFormat DECIMAL_FORMAT;

  // Date format used when serializing dates from/to strings
  private static final DateFormat SERIALIZATION_FORMAT;

  // Expression evaluator, "singleton" used accross all instances
  private static final IExpressionEvaluator evaluator;

  static {
    DECIMAL_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    DECIMAL_FORMAT.applyPattern("0.00");
    SERIALIZATION_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    evaluator = new ExpressionEvaluator();
  }

  ///////////////////////////////// Properties ////////////////////////////////

  // Unmodified items read from the config
  private final List<Object> items;

  // Variable names and their values that need to be substituted
  // Names are translated to a pattern when added to the instance
  @Getter private final Map<String, String> variables;

  // Lookup table resolver of the config this value came from
  @Getter private final @Nullable ILutResolver lutResolver;

  // Global prefix value
  @Setter private String prefix;

  // Prefix mode, 'N' = none, 'F' = first line, 'A' = all lines
  private char prefixMode;

  // Whether color code translation is enabled
  @Getter
  @Accessors(fluent = true)
  private boolean areColorsEnabled;

  /**
   * Create a new config value builder by a value
   * @param val Value
   * @param lutResolver Global lookup table resolver
   */
  public ConfigValue(Object val, @Nullable ILutResolver lutResolver) {
    this(List.of(val), lutResolver);
  }

  /**
   * Create a new config value builder containing multiple items
   * @param items List of items
   * @param lutResolver Global lookup table resolver
   */
  public ConfigValue(List<Object> items, @Nullable ILutResolver lutResolver) {
    this.items = new ArrayList<>(items);
    this.lutResolver = lutResolver;
    this.prefixMode = 'N';
    this.variables = new HashMap<>();
    this.areColorsEnabled = true;
  }

  //=========================================================================//
  //                                 Builder                                 //
  //=========================================================================//

  ////////////////////////////////// Prefix ///////////////////////////////////

  /**
   * Add a prefix to the start of the first line of a resulting string
   */
  public ConfigValue withPrefix() {
    this.prefixMode = 'F';
    return this;
  }

  /**
   * Add a prefix to the start of every line of a resulting string
   */
  public ConfigValue withPrefixes() {
    this.prefixMode = 'A';
    return this;
  }

  /**
   * Disables any automatic color manipulation within values
   */
  public ConfigValue disableColors() {
    this.areColorsEnabled = false;
    return this;
  }

  ///////////////////////////////// Variables /////////////////////////////////

  /**
   * Add a variable to the template of this value
   * @param name Name of the variable
   * @param value Value of the variable
   */
  public ConfigValue withVariable(String name, @Nullable Object value) {
    this.variables.put(
      name.toLowerCase(),
      value == null ? "null" : stringifyVariable(value)
    );
    return this;
  }

  /**
   * Add a variable to the template of this value
   * @param name Name of the variable
   * @param value Value of the variable
   * @param suffix Suffix to add to the value
   */
  public ConfigValue withVariable(String name, @Nullable Object value, String suffix) {
    this.variables.put(
      name.toLowerCase(),
      value == null ? "null" : (stringifyVariable(value) + suffix)
    );
    return this;
  }

  /**
   * Apply an external map of variables all at once
   * @param variables Map of variables
   */
  public ConfigValue withVariables(@Nullable Map<String, String> variables) {
    if (variables != null)
      this.variables.putAll(variables);
    return this;
  }

  /**
   * Export all currently known variables
   * @return Map of variables
   */
  public Map<String, String> exportVariables() {
    return Collections.unmodifiableMap(this.variables);
  }

  /**
   * Join this value with another value in place, by joining all items and
   * variables, where the variables of other may override entries of this.
   * The prefix, the palette and the prefix mode are not updated and
   * remain as they currently are.
   * @param other Value to join with
   * @param condition Contition which has to evaluate to true in order to perform the join
   */
  public ConfigValue joinWith(Supplier<ConfigValue> other, boolean condition) {
    if (!condition)
      return this;

    ConfigValue cv = other.get();
    this.variables.putAll(cv.exportVariables());
    this.items.addAll(cv.items);
    return this;
  }

  //=========================================================================//
  //                                 Builder                                 //
  //=========================================================================//

  /////////////////////////////// Scalar Value ////////////////////////////////

  /**
   * Build as a scalar by stringifying values and joining
   * them using newlines for line separation
   * @return String value
   */
  public String asScalar() {
    return asScalar("\n");
  }

  /**
   * Build as a component by stringifying values and joining
   * them using newlines for line separation
   * @param gradientGenerator Gradient generator ref for generating gradients from gradient notation, optional
   * @return Component value
   */
  public TextComponent asComponent(@Nullable GradientGenerator gradientGenerator) {
    return TextComponent.parseFromText(asScalar(), gradientGenerator);
  }

  /**
   * Get the first available value from the list as a
   * scalar of a specific type by trying to cast
   * @param type Required type
   * @return Cast type or null
   */
  public<T> @Nullable T asScalar(Class<T> type) {
    try {
      if (items.size() != 0)
        return cast(items.get(0), type).orElse(null);
    } catch (ClassCastException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Get the first available value from the list as a
   * scalar of a specific type by trying to cast or use the provided fallback
   * @param type Required type
   * @param fallback Fallback if this value is empty or the cast failed
   * @return Cast type or null
   */
  public<T> T asScalar(Class<T> type, T fallback) {
    try {
      if (items.size() != 0)
        return cast(items.get(0), type).orElseThrow();
    } catch (Exception ignored) {}
    return fallback;
  }

  /**
   * Build as a scalar value using a custom string for line separation
   * @param sep Custom line separator
   * @return String value
   */
  public String asScalar(String sep) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < items.size(); i++) {
      String line = items.get(i).toString();

      // Separate lines
      if (i != 0) {
        // Reset colors between lines
        result.append("§r");
        result.append(sep);
      }

      // Add prefix based on previous selection on non-empty strings
      if (prefixMode == 'F' && i == 0 || prefixMode == 'A' && !line.isBlank())
        result.append(prefix);

      // Append the actual line after evaluation
      result.append(evaluator.evaluate(line, this));
    }

    return result.toString();
  }

  ///////////////////////////////// List Value ////////////////////////////////

  /**
   * Build as a list of strings, as they were defined in the
   * config, but split newlines.
   * @return List of strings
   */
  public List<String> asList() {
    return items.stream()
      .map(line -> evaluator.evaluate(line.toString(), this))
      .filter(line -> !line.isEmpty())
      .map(line -> Arrays.asList(line.split("\\\\n|\\R")))
      .reduce(new ArrayList<>(), (a, b) -> {
        a.addAll(b);
        return a;
      });
  }

  /**
   * Build as a list of components, as they were defined in the
   * config, but split newlines.
   * @param gradientGenerator Gradient generator ref for generating gradients from gradient notation, optional
   * @return List of components
   */
  public List<TextComponent> asComponentList(@Nullable GradientGenerator gradientGenerator) {
    return asList().stream()
      .map(l -> TextComponent.parseFromText(l, gradientGenerator))
      .collect(Collectors.toList());
  }

  /**
   * Get the local list by casting every element to the provided
   * type, where mismatching entries are skipped
   * @param type Required type
   * @return List of casted values
   */
  public<T> List<T> asList(Class<T> type) {
    List<T> buf = new ArrayList<>();

    for (Object o : items) {
      try {
        cast(o, type).ifPresent(buf::add);
      } catch (ClassCastException ignored) {}
    }

    return buf;
  }

  /**
   * Get the local list as a set by casting every element to the provided
   * type, where mismatching entries are skipped
   * @param type Required type
   * @return Set of casted values
   */
  public<T> Set<T> asSet(Class<T> type) {
    Set<T> buf = new HashSet<>();

    for (Object o : items) {
      try {
        cast(o, type).ifPresent(buf::add);
      } catch (ClassCastException ignored) {}
    }

    return buf;
  }

  //=========================================================================//
  //                              Value Conversion                           //
  //=========================================================================//

  /**
   * Tries to "cast" an object read from the config using get() into the
   * required type and responds with an empty result if the conversion is impossible
   * @param value Value to convert
   * @param type Type to convert to
   */
  private<T> Optional<T> cast(Object value, Class<T> type) {
    try {
      // Requested the whole wrapper
      if (type == ConfigValue.class)
        return Optional.of(type.cast(this));

      // MD5 Component
      if (type == TextComponent.class)
        return Optional.of(type.cast(new TextComponent(asScalar("\n"))));

      // Ensure wrapper types for easier comparison
      Class<T> wType = Primitives.wrap(type);

      // Already a matching data-type
      if (wType.equals(Primitives.wrap(value.getClass())))
        return Optional.of(type.cast(value));

      // Stringify the value like asScalar would
      String stringValue = evaluator.evaluate(value.toString().trim(), this);

      // Requested an abstract material definition
      if (wType == XMaterial.class)
        return XMaterial.matchXMaterial(stringValue).map(wType::cast);

      // Since enchantments are just constants, map here
      if (wType == Enchantment.class)
        return XEnchantment.matchXEnchantment(stringValue).map(xe -> wType.cast(xe.getEnchant()));

      // Since potion types are just constants, map here
      if (wType == PotionType.class) {
        // Try to get the abstracted type
        return XPotion.matchXPotion(stringValue).map(XPotion::getPotionType)
          // Otherwise, try to parse directly
          // Useful for types like AWKWARD, MUNDANE, ...
          .or(() -> {
            try {
              return Optional.of(PotionType.valueOf(stringValue));
            } catch (IllegalArgumentException ignored) {
              return Optional.empty();
            }
          })
          .map(wType::cast);
      }

      // Since potion effect types are just constants, map here
      if (wType == PotionEffectType.class)
        return XPotion.matchXPotion(stringValue).map(xe -> wType.cast(xe.getPotionEffectType()));

      // String value as scalar
      if (wType == String.class)
        return Optional.of(wType.cast(stringValue));

      // Automatic color parsing with RGB-notation support
      if (wType == Color.class) {
        // Try to parse an enum name
        Optional<T> enumValue = parseEnum(wType, stringValue);
        if (enumValue.isPresent())
          return enumValue;

        // Assume it's an RGB color
        String[] parts = stringValue.split(" ");

        // Malformed
        if (parts.length != 3)
          return Optional.empty();

        // Parse RGB parts
        return Optional.of(wType.cast(Color.fromRGB(
          Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]),
          Integer.parseInt(parts[2])
        )));
      }

      // Check if the class has any static constants of it's own type
      // Enums in Bukkit are often made up like this
      boolean hasStaticSelfConstants = (
        Arrays.stream(wType.getDeclaredFields())
          .anyMatch(field -> (
            // Is of self type
            field.getType().equals(wType) &&
            // And is static
            Modifier.isStatic(field.getModifiers()))
          )
      );

      // Automatic enum parsing
      if (wType.isEnum() || hasStaticSelfConstants)
        return parseEnum(wType, stringValue);

      // Parse integers
      if (wType == Integer.class || wType == Long.class)
        return Optional.of(wType.cast(Integer.parseInt(stringValue)));

      // Parse floating points
      if (wType == Float.class || wType == Double.class)
        return Optional.of(wType.cast(Float.parseFloat(stringValue)));

      // Parse booleans
      if (wType == Boolean.class)
        return Optional.of(wType.cast(evaluator.parseBoolean(stringValue)));

      return Optional.of(wType.cast(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Parses an "enum" from either true enum constants or static self-typed
   * constant declarations within the specified class
   * @param c Target class
   * @param value String value to parse
   * @return Optional constant, empty if there was no such constant
   */
  @SuppressWarnings("unchecked")
  private<T> Optional<T> parseEnum(Class<T> c, String value) {
    value = value.trim();

    // Parse enums
    if (c.isEnum()) {
      for (T ec : c.getEnumConstants()) {
        if (((Enum<?>) ec).name().equalsIgnoreCase(value))
          return Optional.of(ec);
      }
    }

    // Parse classes with static constants
    else {
      try {
        List<Field> constants = Arrays.stream(c.getDeclaredFields())
          .filter(field -> field.getType().equals(c) && Modifier.isStatic(field.getModifiers()))
          .collect(Collectors.toList());

        for (Field constant : constants) {
          if (constant.getName().equalsIgnoreCase(value))
            return Optional.of((T) constant.get(null));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    return Optional.empty();
  }

  //=========================================================================//
  //                              Miscellaneous                              //
  //=========================================================================//

  /**
   * Create a carbon copy of this config value
   */
  public ConfigValue copy() {
    return new ConfigValue(new ArrayList<>(items), new HashMap<>(variables), lutResolver, prefix, prefixMode, areColorsEnabled);
  }

  @Override
  public String toString() {
    return asScalar();
  }

  @Override
  public DateFormat getSerializationFormat() {
    return SERIALIZATION_FORMAT;
  }

  //=========================================================================//
  //                         Variable Stringification                        //
  //=========================================================================//

  /**
   * Turn a variable's value into it's string representation
   * @param value Value to stringify
   */
  private static String stringifyVariable(Object value) {
    // Doubles should always have two decimal digits
    if (value instanceof Double) {
      Double d = (Double) value;
      return d == 0 ? "0" : DECIMAL_FORMAT.format(d);
    }

    // Floats should always have two decimal digits
    if (value instanceof Float) {
      Float f = (Float) value;
      return f == 0 ? "0" : DECIMAL_FORMAT.format(f);
    }

    // Serialize dates
    if (value instanceof Date)
      return SERIALIZATION_FORMAT.format((Date) value);

    return value.toString();
  }

  //=========================================================================//
  //                           Convenience Creators                          //
  //=========================================================================//

  /**
   * Directly export a single variable
   */
  public static Map<String, String> singleVariable(String key, Object value) {
    return Map.of(key.toLowerCase(), stringifyVariable(value));
  }

  /**
   * Make a new empty instance
   */
  public static ConfigValue makeEmpty() {
    return new ConfigValue("", null);
  }

  /**
   * Make a new instance from an immediate value
   * @param value Immediate value
   */
  public static ConfigValue immediate(String value) {
    return new ConfigValue(value, null);
  }
}
