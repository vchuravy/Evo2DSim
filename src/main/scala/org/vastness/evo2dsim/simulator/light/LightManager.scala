package org.vastness.evo2dsim.simulator.light

import scala.collection.mutable.ArrayBuffer

class LightManager {
    var lightSources = ArrayBuffer[LightSource]()

    def addLight(l: LightSource){
      lightSources += l
    }
}
