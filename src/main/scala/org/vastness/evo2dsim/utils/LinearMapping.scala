/*
 * This file is part of Evo2DSim.
 *
 * Evo2DSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vastness.evo2dsim.utils

import spire.implicits._
import spire.math._

trait LinearMapping {
  def UPPER_OUTPUT_LIMIT: Rational
  def LOWER_OUTPUT_LIMIT: Rational

  def UPPER_INPUT_LIMIT: Rational
  def LOWER_INPUT_LIMIT: Rational

  @inline
  def a = (UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT) / (UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)

  @inline
  def b = UPPER_OUTPUT_LIMIT - UPPER_INPUT_LIMIT*(UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT) / (UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)

  @inline
  def transform(x: Rational) = (a * x)  + b //Linear transformation

}
