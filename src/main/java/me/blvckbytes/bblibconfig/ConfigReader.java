package me.blvckbytes.bblibconfig;

import com.google.common.primitives.Primitives;
import me.blvckbytes.bblibconfig.sections.CSAlways;
import me.blvckbytes.bblibconfig.sections.CSIgnore;
import me.blvckbytes.bblibconfig.sections.CSMap;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.bukkit.configuration.MemorySection;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/22/2022

  A wrapper for the basic IConfig which provides reading complex
  values by their key and handles all possible cases when parsing.

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
public class ConfigReader {

  private final IConfig cfg;
  private final String path;
  private final @Nullable ILogger logger;
  private final Map<String, Object> parseCache;

  public ConfigReader(
    IConfig cfg, String path,
    @Nullable ILogger logger
  ) {
    this.cfg = cfg;
    this.path = path;
    this.logger = logger;
    this.parseCache = new HashMap<>();
  }

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  /**
   * Parse a configuration section into a matching internal model by reviving
   * known data-types and walking the whole data-structure recursively, if necessary
   * @param key Key to start parsing from (inclusive), null means top level (full file)
   * @param type Internal model to parse into
   * @return Optional parsed model, if the key existed
   */
  @SuppressWarnings("unchecked")
  public<T> Optional<T> parseValue(@Nullable String key, Class<T> type, boolean cache) {
    if (cache && parseCache.containsKey(key))
      return Optional.of((T) parseCache.get(key));

    return parseValueSub(key, type, null, false, false)
      .map(v -> {
        if (cache)
          parseCache.put(key, v);
        return v;
      });
  }

  /**
   * Recursive sub-routine with extra parameters
   */
  @SuppressWarnings("unchecked")
  private<T> Optional<T> parseValueSub(@Nullable String key, Class<T> type, Field f, boolean withinArray, boolean ignoreMissing) {
    boolean isSection = AConfigSection.class.isAssignableFrom(type);

    // Null keys mean root level scope
    String cKey = key == null ? "" : key;

    if (
      // Does not exist
      cfg.get(path, cKey).isEmpty() &&
      // And is either within an array (missing = stop condition), or missing is not being ignored
      (!ignoreMissing || withinArray)
    )
      return Optional.empty();

    // Is a map, parse all keys of this section
    if (Map.class.isAssignableFrom(type)) {
      CSMap kvInfo = f == null ? null : f.getAnnotation(CSMap.class);
      Map<Object, Object> items = new HashMap<>();

      // If there is either no annotation present or the
      // value was parsed directly without a wrapping class, use string maps
      Class<?> kC = kvInfo == null ? String.class : kvInfo.k();
      Class<?> vC = kvInfo == null ? String.class : kvInfo.v();

      ConfigValue cv = get(cKey).orElse(null);

      // Value unavailable
      if (cv == null)
        return Optional.of(type.cast(items));

      // Try to interpret as a map directly
      Map<?, ?> map = cv.asScalar(Map.class);
      if (map != null) {
        map.forEach((k, v) -> {
          // Value type unparsable
          Object pV = parseValue(join(cKey, k.toString()), vC, false).orElse(null);
          if (pV == null)
            return;

          items.put(kC.cast(k), pV);
        });

        return Optional.of(type.cast(items));
      }

      // Try to interpret as a memory section
      MemorySection ms = cv.asScalar(MemorySection.class);
      if (ms != null) {
        // Iterate all keys of this section
        for (String msKey : ms.getKeys(false)) {
          // Value type unparsable
          Object v = parseValue(join(cKey, msKey), vC, false).orElse(null);
          if (v == null)
            continue;

          // Key type unparsable
          Object parsedKey = ConfigValue.immediate(msKey).asScalar(kC);
          if (parsedKey == null)
            continue;

          items.put(parsedKey, v);
        }
      }

      return Optional.of(type.cast(items));
    }

    // Is an array, multiple elements of the same type in a sequence
    if (type.isArray() || List.class.isAssignableFrom(type)) {
      Class<?> arrType = type.getComponentType();

      // Try to fetch as many values of the list as possible, until the end is reached
      List<Object> items = new ArrayList<>();
      for (int i = 0; i < Integer.MAX_VALUE; i++) {
        Optional<?> v = parseValueSub(cKey + "[" + i + "]", (Class<? extends AConfigSection>) arrType, f, true, false);

        // End of list reached, no more items available
        if (v.isEmpty())
          break;

        items.add(v.get());
      }

      // Only set if there are actually items available
      if (items.size() > 0)
        return Optional.of(
          type.isArray() ?
            // Store as array
            type.cast(items.toArray((Object[]) Array.newInstance(arrType, 1))) :
            // Store as list
            type.cast(items)
        );

      return Optional.empty();
    }

    // Is a configuration section, which means each field will be parsed
    // separately, supporting for recursion
    if (isSection) {
      try {
        AConfigSection res = (AConfigSection) type.getConstructor().newInstance();

        List<Field> fields = findFields(type).stream()
          .sorted((a, b) -> {
            if (a.getType() == Object.class && b.getType() == Object.class)
              return 0;

            // Objects are "greater", so they'll be last when sorting ASC
            return a.getType() == Object.class ? 1 : -1;
          })
          .collect(Collectors.toList());

        for (Field field : fields) {
          // Ignore fields marked for ignore
          if (field.getAnnotation(CSIgnore.class) != null)
            continue;

          field.setAccessible(true);

          // A field is marked as always by either being directly marked, or by being the member of a marked class
          boolean isAlways = field.isAnnotationPresent(CSAlways.class) || type.isAnnotationPresent(CSAlways.class);

          String fName = field.getName();
          Class<?> fType = field.getType();
          String fKey = join(cKey, fName);

          // Try to transform the type by letting the class decide at runtime
          if (fType == Object.class)
            fType = res.runtimeDecide(fName);

          // Is another config section and thus needs recursion
          if (AConfigSection.class.isAssignableFrom(fType)) {

            Object v = parseValueSub(fKey, (Class<? extends AConfigSection>) fType, field, false, isAlways).orElse(null);
            if (v != null)
              field.set(res, v);

            continue;
          }

          // Initially try to parse the value
          Class<?> ffType = fType;
          Object v = parseValueSub(fKey, fType, field, false, false).orElse(null);

          // Failed, try to ask for a default value
          if (v == null)
            v = res.defaultFor(ffType, fName);

          // Still null and has always flag, create an empty collection of length zero
          if (v == null && isAlways) {
            if (fType.isArray())
              v = Array.newInstance(fType.getComponentType(), 0);
            else if (List.class.isAssignableFrom(fType))
              v = new ArrayList<>();
          }

          if (v != null)
            field.set(res, v);
        }

        // Done, no more changes are to be made
        res.afterParsing(fields);

        return Optional.of(type.cast(res));
      } catch (Exception e) {
        if (logger == null)
          e.printStackTrace();
        else
          logger.logError(e);
      }
    }

    // Since ConfigValue scalars always work with boxed types, box at this point
    type = Primitives.wrap(type);

    // Try to use ConfigValue's internal casting mechanism
    ConfigValue cv = get(cKey).orElse(null);
    if (cv != null) {
      // Set the scalar value, only if it's type matches
      Object v = cv.asScalar(type);
      if (v != null && type.isAssignableFrom(v.getClass()))
        return Optional.of(type.cast(v));
    }

    return Optional.empty();
  }

  /**
   * Get a config-value from the file of this reader
   * @param key Key to retrieve
   */
  public Optional<ConfigValue> get(String key) {
    return cfg.get(path, key);
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  /**
   * Finds all fields of a class while accounting for inheritance
   * @param c Target class
   * @return List of found fields
   */
  private List<Field> findFields(Class<?> c) {
    List<Field> res = new ArrayList<>();

    // Walk superclass hierarchy and collect all fields
    Class<?> curr = c;
    while (curr != null && curr != Object.class) {
      res.addAll(Arrays.asList(curr.getDeclaredFields()));
      curr = curr.getSuperclass();
    }

    return res;
  }

  /**
   * Join two keys with a separating dot and handle all cases
   * @param keyA Key A of the result
   * @param keyB Key B of the result
   * @return A concatenated with B
   */
  private String join(String keyA, String keyB) {
    if (keyA.isBlank())
      return keyB;

    if (keyB.isBlank())
      return keyA;

    if (keyA.endsWith(".") && keyB.startsWith("."))
      return keyA + keyB.substring(1);

    if (keyA.endsWith(".") || keyB.startsWith("."))
      return keyA + keyB;

    return keyA + "." + keyB;
  }
}