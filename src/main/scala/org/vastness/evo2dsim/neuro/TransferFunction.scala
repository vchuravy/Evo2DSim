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
import org.apache.commons.math3.util.FastMath

sealed trait TransferFunction {
  def name: String
  def apply(x: NumberT): NumberT
}

object TransferFunction extends Enum[TransferFunction] {
  case object THANH extends TransferFunction {
    val name = "thanh"
    def apply(activity: NumberT) = FastMath.tanh(activity)
  }
  case object SIG extends TransferFunction {
    val name = "sig"
    def apply(activity: NumberT) =   1 / (1 + FastMath.exp(-activity))
  }
  case object BINARY extends TransferFunction {
    val name = "binary"
    def apply(activity: NumberT) = if (activity >= zero) one else zero
  }
}