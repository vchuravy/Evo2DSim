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

package org.vastness.evo2dsim.neuro

import org.vastness.utils.Enum
import spire.math._
import spire.implicits._
import spire.algebra._
import java.math.MathContext

sealed trait TransferFunction {
  def name: String
  def apply(x: Rational): Rational
}

object TransferFunction extends Enum[TransferFunction] {
  implicit val mc = new MathContext(10)
  case object THANH extends TransferFunction {
    val name = "thanh"
    def apply(activity: Rational) = tanh(activity.toBigDecimal)
  }
  case object SIG extends TransferFunction {
    val name = "sig"
    def apply(activity: Rational) =   1 / exp(activity.toBigDecimal)
  }
  case object BINARY extends TransferFunction {
    val name = "binary"
    def apply(activity: Rational) = if (activity >= 0) 1 else 0
  }
}