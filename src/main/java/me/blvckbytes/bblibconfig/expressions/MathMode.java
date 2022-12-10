package me.blvckbytes.bblibconfig.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiFunction;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/10/2022

  Lists all available math modes and their evaluation functions.

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
public enum MathMode {
  PLUS(Double::sum),
  MINUS((a, b) -> a - b),
  MULTIPLY((a, b) -> a * b),
  DIVIDE((a, b) -> a / b),
  POW(Math::pow),
  ;

  private final BiFunction<Double, Double, Double> calculate;
}
