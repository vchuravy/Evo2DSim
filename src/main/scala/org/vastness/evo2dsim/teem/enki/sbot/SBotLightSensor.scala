/*
 * This file is part of Evo2DSim.
 *
 * Evo2DSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.neuro.{TransferFunction, SensorNeuron, Neuron}
import org.vastness.evo2dsim.simulator.light.LightSource
import org.vastness.evo2dsim.gui.Color
import org.apache.commons.math3.util.FastMath


class SBotLightSensor(segments: Int, bias: Double) {
  val fov = 360
  val resolution = 4 * fov
  var debug = false
  private var agent: Option[SBot] = None
  private val redNeurons = new Array[Neuron](segments)
  private val blueNeurons = new Array[Neuron](segments)

  assert(resolution%segments == 0)
  val pixels = resolution/segments

  createNeurons()


  private var visionStrip = Map.empty[Color, Array[Float]]

  clear()
  def getVisionStrip = visionStrip

  def clear() {
    visionStrip = Map(Color.RED -> new Array[Float](resolution), Color.BLUE -> new Array[Float](resolution))
  }

  /**
   * Fills visionStrip, if light from a source falls onto the area.
   * A point light source (in the center of the object that emits the light) shines light on the surface of an object
   * based upon the distance and relative position to the target.
   * The code is take from enki
   */
  def calcVision(sBot: SBot) {
    clear()
    for(light: LightSource <- sBot.sim.lightManager.lightSources){
      if (light.active && sBot.light != light && light.radius > 0){
        val radius = light.radius
        // Alternative way to compute the same thing.
        // val vec = light.position sub sBot.position // From body to light
        // val distance = vec.normalize() // returns length of vector and normalizes it
        // val lightPosition = sBot.body.getLocalVector(vec)
        val lightPosition = sBot.body.getLocalPoint(light.position)
        val distance = lightPosition.length() - radius
        lightPosition.normalize()

        val aperture    = FastMath.atan(radius / distance)
        val bearingRad  = FastMath.atan2(lightPosition.x, lightPosition.y) // clockwise angle
        val beginAngle  = FastMath.toDegrees(bearingRad - aperture)
        val endAngle    = FastMath.toDegrees(bearingRad + aperture)

        // Calculation taken from Enki
        val start: Int = FastMath.floor((resolution - 1) * 0.5 * (beginAngle / fov + 1)).toInt
        val end: Int = FastMath.ceil((resolution - 1) * 0.5 * (endAngle / fov + 1)).toInt

		  	for(i <- start to end){
          visionStrip(light.color)(i % resolution) = 1 //TODO: fog, noise, objects standing in sight?
        }
      }
    }
  }

  private def createNeurons() {
    for( i <- 0 until segments){
      blueNeurons(i) = new SensorNeuron(bias, TransferFunction.THANH, getAverage(Color.BLUE, i) )
      redNeurons(i) = new SensorNeuron(bias, TransferFunction.THANH, getAverage(Color.RED, i))
    }
  }

  @inline
  def getAverage(c: Color, index: Int)(): Double = visionStrip(c).view(pixels*index, pixels*(index+1)).sum / pixels

  def getNeurons = (blueNeurons ++ redNeurons).toList

  def attachToAgent(sBot: SBot){
    this.agent = Some(sBot)
  }
  def step() {
    agent match {
      case Some(sBot) => calcVision(sBot)
      case None => {}
    }
  }

}
