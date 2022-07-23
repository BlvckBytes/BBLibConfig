package me.blvckbytes.bblibconfig;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/25/2022

  Represents a configuration value and offers multiple
  parsing and conversion features.
*/
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigValue {

  // Decimal format used when encountering double variables
  private static final DecimalFormat DECIMAL_FORMAT;

  static {
    DECIMAL_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    DECIMAL_FORMAT.applyPattern("0.00");
  }

  // Unmodified lines read from the config
  private final List<Object> lines;

  // Variable names and their values that need to be substituted
  // Names are translated to a pattern when added to the instance
  private final Map<String, String> vars;

  private final @Nullable ILutResolver lutResolver;

  // Global prefix value
  @Setter
  private String prefix;

  // Prefix mode, 'N' = none, 'F' = first line, 'A' = all lines
  private char prefixMode;

  private boolean disableColors;

  /**
   * Create a new config value builder by a value
   * @param val Value
   * @param lutResolver Global lookup table resolver
   */
  public ConfigValue(Object val, @Nullable ILutResolver lutResolver) {
    this(List.of(val), lutResolver);
  }

  /**
   * Create a new config value builder by multiple lines
   * @param lines List of lines
   * @param lutResolver Global lookup table resolver
   */
  public ConfigValue(List<Object> lines, @Nullable ILutResolver lutResolver) {
    this.lines = new ArrayList<>(lines);
    this.lutResolver = lutResolver;
    this.prefixMode = 'N';
    this.vars = new HashMap<>();
  }

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
    this.disableColors = true;
    return this;
  }

  /**
   * Add a variable to the template of this value
   * @param name Name of the variable
   * @param value Value of the variable
   */
  public ConfigValue withVariable(String name, @Nullable Object value) {
    this.vars.put(
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
    this.vars.put(
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
      this.vars.putAll(variables);
    return this;
  }

  /**
   * Export all currently known variables
   * @return Map of variables
   */
  public Map<String, String> exportVariables() {
    return Collections.unmodifiableMap(this.vars);
  }

  /**
   * Join this value with another value in place, by joining all lines and
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
    this.vars.putAll(cv.exportVariables());
    this.lines.addAll(cv.lines);
    return this;
  }

  /**
   * Build as a scalar by stringifying values and joining
   * them using newlines for line separation
   * @return String value
   */
  public String asScalar() {
    return asScalar("\n");
  }

  /**
   * Get the first available value from the list as a
   * scalar of a specific type by trying to cast
   * @param type Required type
   * @return Cast type or null
   */
  public<T> @Nullable T asScalar(Class<T> type) {
    try {
      if (lines.size() != 0)
        return cast(lines.get(0), type).orElse(null);
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
      if (lines.size() != 0)
        return cast(lines.get(0), type).orElseThrow();
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

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).toString();

      // Separate lines
      if (i != 0) {
        // Reset colors between lines
        result.append("§r");
        result.append(sep);
      }

      // Add prefix based on previous selection on non-empty strings
      if (prefixMode == 'F' && i == 0 || prefixMode == 'A' && !line.isBlank())
        result.append(prefix);

      // Append the actual line after transformation
      result.append(transformLine(line));
    }

    return result.toString();
  }

  /**
   * Build as a component using a custom string for line separation
   * @param sep Custom line separator
   * @return Component value
   */
  public TextComponent asComponent(String sep) {
    return new TextComponent(asScalar(sep));
  }

  /**
   * Build as a component using newlines for line separation
   * @return Component value
   */
  public TextComponent asComponent() {
    return asComponent("\n");
  }

  /**
   * Build as a list of strings, as they were defined in the config.
   * This means that a scalar results in a list of length 1
   * @return List of strings
   */
  public List<String> asList() {
    return lines.stream()
      .map(line -> this.transformLine(line.toString()))
      .filter(line -> !line.isEmpty())
      .map(line -> Arrays.asList(line.split("\\\\n|\\R")))
      .reduce(new ArrayList<>(), (a, b) -> {
        a.addAll(b);
        return a;
      });
  }

  /**
   * Get the local list by casting every element to the provided
   * type, where mismatching entries are skipped
   * @param type Required type
   * @return List of casted values
   */
  public<T> List<T> asList(Class<T> type) {
    List<T> buf = new ArrayList<>();

    for (Object o : lines) {
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

    for (Object o : lines) {
      try {
        cast(o, type).ifPresent(buf::add);
      } catch (ClassCastException ignored) {}
    }

    return buf;
  }

  /**
   * Shorthand for {@link #asList()}, just in the format of a stream
   * @return List of strings
   */
  public Stream<String> asStream() {
    return asList().stream();
  }

  /**
   * Transform a line into a state where it can be handed back to the
   * caller (replaces colors and variables)
   * @param input Input string
   * @return Transformed result
   */
  private String transformLine(String input) {
    // Translate the color codes first, since no variables should ever introduce color.
    // Then apply the palette and last but not least the variables, so they don't get transformed.
    return applyVariables(applyColors(input), vars);
  }

  /**
   * Substitutes all registered variables into the string's placeholders
   * @param input Input string
   * @param vars Available variables
   * @return Transformed result
   */
  private String applyVariables(String input, Map<String, String> vars) {
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
        String value = resolveVariable(name, vars);

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

  /**
   * Applies all color notations on an input string
   * @param input Input string
   * @return String with all colors applied
   */
  private String applyColors(String input) {
    if (disableColors)
      return input;

    return ChatColor.translateAlternateColorCodes('&', input);
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
   * Resolve the final value of a variable after all required processing
   * @param expr Name of the variable
   * @param vars Available variables
   * @return Final value, empty if that variable is unknown or it's
   * expression could not be processed
   */
  private @Nullable String resolveVariable(String expr, Map<String, String> vars) {
    expr = expr.trim();

    // Immediate string value, also support for inner variables and escaped quotes
    if (
      (expr.startsWith("\"") && expr.endsWith("\"")) ||
      (expr.startsWith("'") && expr.endsWith("'"))
    )
      return applyVariables(removeQuotation(expr), vars);

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

        String valueV = resolveVariable(v.substring(indexEq + 1).trim(), vars);
        String nameV = resolveVariable(v.substring(0, indexEq), vars);

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

        String vV = resolveVariable(v, vars);

        // Name not found
        if (vV == null)
          return null;

        bool = booleanFromString(vV) ^ invert;
      }

      // Cut true and false cases
      String vT = expr.substring(indexQ + 1, indexC);
      String vF = expr.substring(indexC + 1);

      return bool ? resolveVariable(vT, vars) : resolveVariable(vF, vars);
    }

    if (
      // Is a lookup table expression
      indexS >= 0 && indexE >= 0 && lutResolver != null &&
      // And the boolean expression comes first
      (indexS < indexQ || indexQ < 0)
    ) {
      String lutName = expr.substring(0, indexS);
      String keyName = resolveVariable(expr.substring(indexS + 1, indexE), vars);

      // LUT not found
      Map<String, String> lut = lutResolver.getLut(lutName).orElse(null);
      if (lut == null || keyName == null)
        return null;

      // Key not found
      String res = lut.get(keyName);
      if (res == null)
        return null;

      return applyColors(res);
    }

    // Is a loop mapper expression
    // Syntax: {{ <var> %<sep> "<iter value>" }}
    int indexP = realIndexOf(expr, '%');
    if (indexP > 0 && indexP < expr.length() - 1) {
      String varVal = vars.get(expr.substring(0, indexP).trim());

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
      Map<String, String> localVars = new HashMap<>(vars);
      for (int i = 0; i < values.length; i++) {
        localVars.put("it", values[i].trim());
        localVars.put("ind", String.valueOf(i));
        res.append(applyVariables(iterValue, localVars));
      }

      return applyColors(res.toString());
    }

    // Just try to lookup as is
    String res = vars.get(expr);
    return res == null ? null : applyColors(res);
  }

  /**
   * Create a carbon copy of this config value
   */
  public ConfigValue copy() {
    return new ConfigValue(new ArrayList<>(lines), new HashMap<>(vars), lutResolver, prefix, prefixMode, disableColors);
  }

  @Override
  public String toString() {
    return asScalar();
  }
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

  /**
   * Tries to "cast" an object read from the config using get() into the
   * required type and responds with an empty result if the conversion is impossible
   * @param value Value to convert
   * @param type Type to convert to
   */
  private<T> Optional<T> cast(Object value, Class<T> type) {
    try {
      // Ensure wrapper types for easier comparison
      Class<T> wType = Primitives.wrap(type);

      String stringValue = applyVariables(value.toString().trim(), vars);

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

      // Requested the whole wrapper
      if (wType == ConfigValue.class)
        return Optional.of(wType.cast(this));

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

      // Automatic enum parsing
      if (wType.isEnum() || hasStaticSelfConstants(wType))
        return parseEnum(wType, stringValue);

      // Parse integers
      if (wType == Integer.class || wType == Long.class)
        return Optional.of(wType.cast(Integer.parseInt(stringValue)));

      // Parse floating points
      if (wType == Float.class || wType == Double.class)
        return Optional.of(wType.cast(Float.parseFloat(stringValue)));

      // Parse booleans
      if (wType == Boolean.class)
        return Optional.of(wType.cast(booleanFromString(stringValue)));

      return Optional.of(wType.cast(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Checks whether a class declares static constants of it's own type (enum-like)
   * @param c Class to check
   */
  private boolean hasStaticSelfConstants(Class<?> c) {
    return Arrays.stream(c.getDeclaredFields()).anyMatch(field -> field.getType().equals(c) && Modifier.isStatic(field.getModifiers()));
  }

  /**
   * Parses a boolean from any given string
   * @param value String value
   * @return Parsed boolean value
   */
  private Boolean booleanFromString(String value) {
    value = value.toLowerCase().trim();
    return value.equals("1") || value.equals("true");
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

    return value.toString();
  }
}
