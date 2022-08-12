package me.blvckbytes.bblibconfig;

import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Map;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Represents the data provider which makes up the context
  when evaluating configuration expression strings.

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
public interface IExpressionDataProvider {

  /**
   * Get the lookup table resolver of the containing scope
   */
  @Nullable ILutResolver getLutResolver();

  /**
   * Get all available variables which may be substituted
   */
  Map<String, String> getVariables();

  /**
   * Get the serialization format for dates which is used when
   * serializing dates into the variable map {@link #getVariables()}
   */
  DateFormat getSerializationFormat();

  /**
   * Get whether color notation should be substituted
   */
  boolean areColorsEnabled();

}
