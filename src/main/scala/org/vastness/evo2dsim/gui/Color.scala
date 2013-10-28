package org.vastness.evo2dsim.gui

import java.awt
import org.vastness.evo2dsim.utils.Enum

/**
 * Singelton that abstracts away the actual color implementation from the runtime
 */


sealed trait Color extends Color.Value { def underlying: awt.Color }

object Color extends Enum[Color] {
  case object BLACK extends Color { val underlying = awt.Color.BLACK }
  case object RED extends Color { val underlying = awt.Color.RED }
  case object BLUE extends Color { val underlying = awt.Color.BLUE }
  case object CYAN extends Color { val underlying = awt.Color.CYAN }
}




