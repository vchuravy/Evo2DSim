package org.vastness.utils.enums

import org.vastness.utils.Enum

sealed trait NamedEnum {val name: String}

case object NamedEnum extends Enum[NamedEnum] {
  case object Value1 extends NamedEnum { val name = "value1"}
  case object Value2 extends NamedEnum { val name = "value1"}
}
