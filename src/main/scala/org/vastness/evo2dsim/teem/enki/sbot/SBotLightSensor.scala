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
    for(l: LightSource <- lm.lightSources){
      if (l.active && sBot.light != l){
        val lightPosition = sBot.body.getLocalPoint(l.position)
        val distance = lightPosition.length()


        val f = (distance - radius) / 2 * distance // http://www.neoprogrammics.com/spheres/visible_fraction_of_surface.php

        lightPosition.normalize()
        val centerPoint = lightPosition.mul(radius) // center point of the light cone

        val halfRange = (f*360)/2
        val centerPointDegree = math.toDegrees(math.atan2(centerPoint.x-360.0, 360.0 - centerPoint.y) + 360.0) % 360.0 //positive angle
        val start = ((centerPointDegree-halfRange) % 360.0).toInt
        val end = ((centerPointDegree+halfRange) % 360.0).toInt

        for(i <-  start to end){
          visionStrip(l.color)(i) = 1 //TODO: fog, noise, objects standing in sight?
        }
    }
    }
  }

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
