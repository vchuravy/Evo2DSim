package org.vastness.evo2dsim.simulator.light

import scala.collection.mutable.ArrayBuffer
import org.vastness.utils.Enum

class LightManager {
  var lightSources = ArrayBuffer[LightSource]()

  def addLight(l: LightSource){
    lightSources += l
  }

  def findByCategory(category: LightCategory) = lightSources.filter(_.category == category)

}

sealed trait LightCategory

object LightCategory extends Enum[LightCategory] {
  case object AgentLight extends LightCategory
  case object FoodSourceLight extends LightCategory
}
