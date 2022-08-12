package me.blvckbytes.bblibconfig;

import java.util.Map;
import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/09/2022

  Describes a global (config-scope) lookup table section resolver.

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
public interface ILutResolver {

  /**
   * Get a lookup table by it's name
   * @param name Name of the target table
   * @return Optional map, empty if the LUT didn't exist
   */
  Optional<Map<String, String>> getLut(String name);

}
