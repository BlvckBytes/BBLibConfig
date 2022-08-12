package me.blvckbytes.bblibconfig;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/24/2022

  The base class of a configuration section value which is
  represented by a POJO model and can be parsed automatically.

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
public abstract class AConfigSection {

  // Has to implement an empty constructor in order to allow
  // for default creation and field-patching afterwards
  public AConfigSection() {}

  /**
   * Called to decide the type of Object fields at runtime,
   * based on previously parsed values of that instance, as
   * it's patched one field at a time. Decideable fields are
   * always read last, so that they have access to other,
   * known type fields in order to decide properly.
   * @param field Target field in question
   * @return Decided type, Object.class means skip
   */
  public Class<?> runtimeDecide(String field) {
    return Object.class;
  }

  /**
   * Called when a field wasn't found within the config and a default could be set
   * @param type Target field's type
   * @param field Target field name
   * @return Value to use as a default
   */
  public @Nullable Object defaultFor(Class<?> type, String field) {
    return null;
  }

  /**
   * Called when parsing of the section is completed
   * and no more changes will be applied
   */
  public void afterParsing(List<Field> fields) throws Exception {}

  /**
   * Patch the prefix on all config values which are not the prefix itself
   * @param prefix Prefix to apply
   * @param fields List of fields to apply to
   */
  public void distributePrefix(String prefix, List<Field> fields) throws Exception {
    fields.stream()
      .filter(f -> !f.getName().equals("prefix") && f.getType() == ConfigValue.class)
      .forEach(f -> {
        try {
          ConfigValue cv = (ConfigValue) f.get(this);
          if (cv != null)
            cv.setPrefix(prefix);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
  }
}
