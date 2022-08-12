package me.blvckbytes.bblibconfig;

import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/23/2022

  Public interfaces which the Configuration provides to other consumers.

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
public interface IConfig {

  /**
   * Get a value by it's config key from any available config file
   * @param path Path of the target config file (no leading slash, no .yml)
   * @param key Key to identify the value
   */
  Optional<ConfigValue> get(String path, String key);

  /**
   * Get an advanced config reader for a given file
   * @param path Path of the target config file (no leading slash, no .yml)
   */
  Optional<ConfigReader> reader(String path);

  /**
   * Get the file global LUT resolver
   * @param path Path of the target config file (no leading slash, no .yml)
   */
  Optional<ILutResolver> getLutResolver(String path);

}
