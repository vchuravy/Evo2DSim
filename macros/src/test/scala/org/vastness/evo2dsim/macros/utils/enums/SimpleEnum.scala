package org.vastness.evo2dsim.macros.utils.enums

import org.vastness.evo2dsim.macros.utils.Enum

sealed trait SimpleEnum

case object SimpleEnum extends Enum[SimpleEnum] {
  case object Value1 extends SimpleEnum
  case object Value2 extends SimpleEnum
}