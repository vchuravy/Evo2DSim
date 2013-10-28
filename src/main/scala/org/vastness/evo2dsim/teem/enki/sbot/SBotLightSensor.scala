package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.neuro.{TransferFunction, SensorNeuron, Neuron}
import org.vastness.evo2dsim.simulator.light.LightSource
import org.vastness.evo2dsim.gui.Color


class SBotLightSensor(sBot: SBot, segments: Int, bias: Double) {
  private val lm = sBot.sim.lightManager
  private val redNeurons = new Array[Neuron](segments)
  private val blueNeurons = new Array[Neuron](segments)

  createNeurons()


  private var visionStrip = Map[Color, Array[Float]]((Color.RED,new Array[Float](360)), (Color.BLUE, new Array[Float](360))) // two colors

  /**
   * Fills visionStrip, if light from a source falls onto the area.
   * A point light source (in the center of the object that emits the light) shines light on the surface of an object
   * based upon the distance and relative position to the target.
   */
  def calcVision() {
    visionStrip = Map[Color, Array[Float]]((Color.RED,new Array[Float](360)), (Color.BLUE, new Array[Float](360))) // clean the last image

    def clamp(x: Float, max: Float) =
      if(x > max) max else x

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
        val bearingDeg = (math.toDegrees(bearingRad)+360) % 360

        val start: Int = ((bearingDeg-halfRange) + 360).round.toInt % 360

        for(i:Int <-  start to (start + 2*halfRange).round){
          visionStrip(light.color)(i % 360) = 1 //TODO: fog, noise, objects standing in sight?
        }

      }
    }
  }

  private def createNeurons(){
    assert(360%segments == 0)
    val pixels = 360/segments
    for( i <- 0 until segments){
      blueNeurons(i) = new SensorNeuron(bias, TransferFunction.thanh, () => visionStrip(Color.BLUE).view(pixels*i,pixels*(i+1)).sum/pixels)
      redNeurons(i) = new SensorNeuron(bias, TransferFunction.thanh, () => visionStrip(Color.RED).view(pixels*i,pixels*(i+1)).sum/pixels)
    }
  }

  def getNeurons = (blueNeurons ++ redNeurons).toList

  def step() {
    calcVision()
  }

}
