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

package org.vastness.evo2dsim.core.gui

import java.awt
import org.vastness.evo2dsim.macros.utils.Enum

/**
 * Singelton that abstracts away the actual color implementation from the runtime
 */


sealed trait Color { def underlying: awt.Color }

object Color extends Enum[Color] {
  case object BLACK extends Color { val underlying = awt.Color.BLACK }
  case object RED extends Color { val underlying = awt.Color.RED }
  case object BLUE extends Color { val underlying = awt.Color.BLUE }
  case object CYAN extends Color { val underlying = awt.Color.CYAN }
}




