package me.blvckbytes.bblibconfig.sections;

import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/20/2022

  Represents all credentials and endpoint information which
  a database connection requires.

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
@Getter
public class DatabaseSection extends AConfigSection {

  private ConfigValue username;
  private ConfigValue password;
  private ConfigValue host;
  private ConfigValue port;
  private ConfigValue database;

  @Override
  public @Nullable Object defaultFor(Class<?> type, String field) {
    if (type == ConfigValue.class)
      return ConfigValue.makeEmpty();

    return super.defaultFor(type, field);
  }
}
