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
  val resolution = 4*360
  val fov = 360
  private var agent: Option[SBot] = None
  private val redNeurons = new Array[Neuron](segments)
  private val blueNeurons = new Array[Neuron](segments)

  createNeurons()


  private var visionStrip = Map.empty[Color, Array[Float]]
  def getVisionStrip = visionStrip
  def clear() {
    visionStrip = Map(Color.RED -> new Array[Float](resolution), Color.BLUE -> new Array[Float](resolution))
  } // clean the last image

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
        val lightPosition = sBot.body.getLocalPoint(light.position)
        val distance = lightPosition.normalize() - light.radius

        val aperture = FastMath.atan(light.radius / distance)
        val bearingRad = FastMath.atan2(lightPosition.x, lightPosition.y) // clockwise angle
        val beginAngle = FastMath.toDegrees(bearingRad - aperture)
        val endAngle = FastMath.toDegrees(bearingRad + aperture)

        val start: Int = FastMath.floor((resolution - 1) * 0.5 * (beginAngle / fov + 1)).toInt
        val end: Int = FastMath.ceil((resolution - 1) * 0.5 * (endAngle / fov + 1)).toInt
        if(light.color == Color.RED) println(f"B:$bearingRad S:$start E:$end SA:$beginAngle EA:$endAngle A:$aperture L:$light")

		  	for(i <- start to end){
            visionStrip(light.color)(i % resolution) = 1 //TODO: fog, noise, objects standing in sight?
        }
      }
    }
  }

  private def createNeurons(){
    assert(resolution%segments == 0)
    val pixels = resolution/segments
    for( i <- 0 until segments){
      blueNeurons(i) = new SensorNeuron(bias, TransferFunction.THANH, () => visionStrip(Color.BLUE).view(pixels*i,pixels*(i+1)).sum/pixels)
      redNeurons(i) = new SensorNeuron(bias, TransferFunction.THANH, () => visionStrip(Color.RED).view(pixels*i,pixels*(i+1)).sum/pixels)
    }
  }

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
