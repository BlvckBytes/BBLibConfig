package me.blvckbytes.bblibconfig;

import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/24/2022

  The base class of a configuration section value which holds
  only string values which should have a human readable error notificaton fallback.

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
public abstract class AConfigStringSection extends AConfigSection {

  /**
   * Called when a field wasn't found within the config and a default could be set
   * @param type Target field's type
   * @param field Target field name
   * @return Value to use as a default
   */
  @Override
  public @Nullable Object defaultFor(Class<?> type, String field) {
    if (type == ConfigValue.class)
      return ConfigValue.immediate("&cundefined");

    if (type == String.class)
      return "§cundefined";

    return null;
  }
}
