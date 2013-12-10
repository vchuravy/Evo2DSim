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

import org.apache.commons.math3.util.FastMath
import org.vastness.utils.Enum

sealed trait TransferFunction {
  def name: String
  def apply(x: Double): Double
}

object TransferFunction extends Enum[TransferFunction] {
  case object THANH extends TransferFunction {
    val name = "thanh"
    def apply(activity: Double) = FastMath.tanh(activity)
  }
  case object SIG extends TransferFunction {
    val name = "sig"
    def apply(activity: Double) =  1/FastMath.pow(math.E, activity)
  }
  case object BINARY extends TransferFunction {
    val name = "binary"
    def apply(activity: Double) = activity match {
      case n if n >= 0 => 1
      case n if n < 0 => 0
    }
  }
}