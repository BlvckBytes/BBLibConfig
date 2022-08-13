package me.blvckbytes.bblibconfig.component;

import com.google.gson.JsonObject;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  The very basic interface of any given component, which can be jsonified.

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
public interface IComponent {

  /**
   * Transforms the components and all of it's properties into
   * the minecraft protocol compliant JSON representation
   * @param approximateColors Whether to approximate HEX colors as ChatColors
   */
  JsonObject toJson(boolean approximateColors);

  /**
   * Get the plain text representation
   */
  String toPlainText();

}
