package org.vastness.evo2dsim.simulator.light

import org.vastness.utils.Enum

sealed trait LightCategory

object LightCategory extends Enum[LightCategory] {
  case object AgentLight extends LightCategory
  case object FoodSourceLight extends LightCategory
}