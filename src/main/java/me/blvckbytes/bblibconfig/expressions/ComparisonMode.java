package me.blvckbytes.bblibconfig.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiFunction;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/09/2022

  Lists all available comparison modes and their evaluation functions.

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
@AllArgsConstructor
public enum ComparisonMode {
  GREATER_THAN((a, b) -> a > b),
  GREATER_THAN_OR_EQUAL((a, b) -> a >= b),
  LESS_THAN((a, b) -> a < b),
  LESS_THAN_OR_EQUAL((a, b) -> a <= b),
  ;

  private final BiFunction<Double, Double, Boolean> compare;
}
