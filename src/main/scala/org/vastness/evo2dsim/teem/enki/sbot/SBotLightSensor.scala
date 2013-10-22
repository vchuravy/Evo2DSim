package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.neuro.{TransferFunction, SensorNeuron, Neuron}
import org.vastness.evo2dsim.simulator.light.LightSource


class SBotLightSensor(sBot: SBot, segments: Int, bias: Double) {
  private val lm = sBot.sim.lightManager
  private val redNeurons = new Array[Neuron](segments)
  private val blueNeurons = new Array[Neuron](segments)

  createNeurons()


  private var visionStrip = Array[Array[Float]](new Array[Float](360), new Array[Float](360)) // two colors

  /**
   * Fills visionStrip, if light from a source falls onto the area.
   * A point light source (in the center of the object that emits the light) shines light on the surface of an object
   * based upon the distance and relative position to the target.
   */
  def calcVision() {
    visionStrip = Array[Array[Float]](new Array[Float](360), new Array[Float](360)) // clean the last image

    val radius = sBot.radius
    for(light: LightSource <- lm.lightSources){
      if (light.active && sBot.light != light){
        val lightPosition = sBot.body.getLocalPoint(light.position)
        val distance = lightPosition.length()

        val f = clamp((distance - radius) / 2 * distance,0.5f) // http://www.neoprogrammics.com/spheres/visible_fraction_of_surface.php

        lightPosition.normalize()
        val centerPoint = lightPosition.mul(radius) // center point of the light cone

        val halfRange = (f*360)/2
        val bearingRad = math.atan2(centerPoint.x, centerPoint.y) // clockwise angle
        val bearingDeg = (math.toDegrees(bearingRad)+360) %360

        val start = ((bearingDeg-halfRange) + 360).round % 360
        val end = ((bearingDeg+halfRange) + 360).round % 360

        var s: Int = 0
        var e: Int = 0

        if (start <= end){
          s = start.toInt
          e = end.toInt
        } else {
          s = end.toInt
          e = start.toInt
        }

        assert(e < 360, "s: " + s + " e: " + e)
        assert(s >= 0,  "s: " + s + " e: " + e)
        assert(s <= e,  "s: " + s + " e: " + e)

        for(i:Int <-  s to e){
            visionStrip(light.color)(i) = 1 //TODO: fog, noise, objects standing in sight?
        }

      }
    }
  }

  private def clamp(x: Float, max: Float) =
    if(x > max) max else x

  private def createNeurons(){
    assert(360%segments == 0)
    val pixels = 360/segments
    for( i <- 0 until segments){
      blueNeurons(i) = new SensorNeuron(bias, TransferFunction.thanh, () => visionStrip(0).view(pixels*i,pixels*(i+1)).sum/pixels)
      redNeurons(i) = new SensorNeuron(bias, TransferFunction.thanh, () => visionStrip(1).view(pixels*i,pixels*(i+1)).sum/pixels)
    }
  }

  def getNeurons = (blueNeurons ++ redNeurons).toList

  def step() {
    calcVision()
  }

}
