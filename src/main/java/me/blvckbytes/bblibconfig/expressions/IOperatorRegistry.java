package me.blvckbytes.bblibconfig.expressions;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Represents the central registry which maps operation types to their implementations.

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
public interface IOperatorRegistry {

  /**
   * Registers a new operation in connection to it's corresponding operator
   * @param type Operator to register for
   * @param operation Operation to execute
   */
  void register(ExpressionOperation type, AOperation operation);

}
