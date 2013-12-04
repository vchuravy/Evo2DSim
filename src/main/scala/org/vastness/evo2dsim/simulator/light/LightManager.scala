package org.vastness.evo2dsim.simulator.light

import scala.collection.mutable.ArrayBuffer

class LightManager {
  var lightSources = ArrayBuffer[LightSource]()
  var disabledCategories = Set.empty[LightCategory]

  def addLight(l: LightSource){
    lightSources += l
  }


  def disableCategory(c: LightCategory) {
    disabledCategories += c
  }

  def enableCategory(c: LightCategory) {
    disabledCategories -= c
  }

  def toggleCategory(c: LightCategory) {
    if (disabledCategories.contains(c)) enableCategory(c)
    else disableCategory(c)
  }
}
