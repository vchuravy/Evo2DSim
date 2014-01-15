package org.vastness.evo2dsim.macros.utils

import org.scalatest._
import org.vastness.evo2dsim.macros.utils.enums._

class EnumTest extends FlatSpec with Matchers {

  "Empty enum" should "have no values" in {
    EmptyEnum.values should be (Set.empty)
  }

  "Simple enum" should "have some values" in {
    SimpleEnum.values should not be Set.empty
  }

  "Simple enum" should "have two values" in {
    SimpleEnum.values.size should be (2)
  }

  "Simple enum" should "have two distinct values" in {
    SimpleEnum.values should be (Set(SimpleEnum.Value1, SimpleEnum.Value2))
  }

  "Named enum" should "have some values" in {
    NamedEnum.values should not be Set.empty
  }

  "Named enum" should "have two values" in {
    NamedEnum.values.size should be (2)
  }

  "Named enum" should "have two distinct values" in {
    NamedEnum.values should be (Set(NamedEnum.Value1, NamedEnum.Value2))
  }
}
