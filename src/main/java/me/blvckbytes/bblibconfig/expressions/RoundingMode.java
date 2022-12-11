package me.blvckbytes.bblibconfig.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiFunction;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 12/10/2022

  Lists all available rounding modes and their evaluation functions.

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
public enum RoundingMode {

  // FIXME: This is not properly guaranteeing nd decimal places

  NONE((n, nd) -> {
    double multiplier = Math.pow(10, nd);
    return Math.floor((n * multiplier)) / (multiplier == 0 ? 1 : multiplier);
  }),

  UP((n, nd) -> {
    double multiplier = Math.pow(10, nd);
    return Math.ceil(n * multiplier) / (multiplier == 0 ? 1 : multiplier);
  }),

  DOWN((n, nd) -> {
    double multiplier = Math.pow(10, nd);
    return Math.floor(n * multiplier) / (multiplier == 0 ? 1 : multiplier);
  }),

  HALF((n, nd) -> {
    double multiplier = Math.pow(10, nd);
    return Math.round(n * multiplier) / (multiplier == 0 ? 1 : multiplier);
  }),
  ;

  // function(number, number_of_decimals): result
  private final BiFunction<Double, Integer, Double> round;
}
