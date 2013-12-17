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

import org.vastness.evo2dsim.neuro._
import org.vastness.evo2dsim.simulator.light.LightSource
import org.vastness.evo2dsim.gui.Color
import org.apache.commons.math3.util.FastMath
import org.vastness.evo2dsim.utils.LinearMapping
import spire.implicits._

class SBotLightSensor(segments: Int, bias: Double) extends LinearMapping {
  val fov = 360
  val resolution = fov
  var debug = false
  private var agent: Option[SBot] = None
  private val redNeurons = new Array[Neuron](segments)
  private val blueNeurons = new Array[Neuron](segments)

  assert(resolution%segments == 0)
  val pixels = resolution/segments

  val UPPER_OUTPUT_LIMIT = 4.0
  val LOWER_OUTPUT_LIMIT = -4.0
  val UPPER_INPUT_LIMIT = pixels.toDouble
  val LOWER_INPUT_LIMIT = 0.0

  createNeurons()

  @inline
  def visionStrip: PartialFunction[Color, Array[Int]] = {
    case Color.RED => red
    case Color.BLUE => blue
  }


  private var red:  Array[Int] = Array.empty
  private var blue: Array[Int] = Array.empty

  @inline
  def sensorValue: PartialFunction[Color, Array[NumberT]] = {
    case Color.RED => redSensorValues
    case Color.BLUE => blueSensorValues
  }

  var redSensorValues = new Array[NumberT](segments)
  var blueSensorValues = new Array[NumberT](segments)


  def getVisionStrip: Map[Color, Array[Int]] = ( Seq(Color.RED, Color.BLUE) collect
    {case c: Color if visionStrip.isDefinedAt(c) => c -> visionStrip(c)} ).toMap

  /**
   * Fills visionStrip, if light from a source falls onto the area.
   * A point light source (in the center of the object that emits the light) shines light on the surface of an object
   * based upon the distance and relative position to the target.
   * The code is take from enki
   */
  def calcVision(sBot: SBot) {
    val redT: Array[Int]   = new Array(resolution)
    val blueT: Array[Int]  = new Array(resolution)

    @inline val visionStripT: PartialFunction[Color, Array[Int]] = {
      case Color.RED => redT
      case Color.BLUE => blueT
    }

    for(light: LightSource <- sBot.sim.lightManager.lightSources){
      if (light.active && sBot.light != light && light.radius > 0){
        val radius = light.radius
        val lightPosition = sBot.body.getLocalPoint(light.position)
        val distance = lightPosition.normalize() - radius


        val aperture    = FastMath.atan(radius / distance)
        val bearingRad  = FastMath.atan2(lightPosition.x, lightPosition.y) // clockwise angle
        val beginAngle  = FastMath.toDegrees(bearingRad - aperture)
        val endAngle    = FastMath.toDegrees(bearingRad + aperture)

        // Calculation taken from Enki
        val start: Int = FastMath.floor((resolution - 1) * 0.5 * (beginAngle / fov + 1)).toInt
        val end: Int = FastMath.ceil((resolution - 1) * 0.5 * (endAngle / fov + 1)).toInt

        //visionStrip(light.color).view(start, end) := 1 //TODO: fog, noise, objects standing in sight?
        cfor(start)(_ <= end, _ + 1) { i =>
          visionStripT(light.color)(i) = 1
        }
      }
    }
    redSensorValues = calculateSensorValues(redT)
    blueSensorValues = calculateSensorValues(blueT)
    red = redT
    blue = blueT
  }

  @inline
  def calculateSensorValues(array: Array[Int]): Array[NumberT] = {
    val t = new Array[NumberT](segments)
    cfor(0)( _ < segments, _ + 1) { i =>
      val start = pixels * i
      val end = pixels * (i + 1)

      t(i) = transform(sum(array, start, end))
    }
    t
  }

  private def createNeurons() {
    for( i <- 0 until segments){
      blueNeurons(i) = new SensorNeuron(bias, TransferFunction.SIG, getSensorFunc(Color.BLUE, i) )
      redNeurons(i) = new SensorNeuron(bias, TransferFunction.SIG, getSensorFunc(Color.RED, i))
    }
  }

  @inline
  def getSensorFunc(c: Color, index: Int): () => NumberT = {
    () => {
      sensorValue(c)(index)
    }
  }

  @inline
  private def sum(array: Array[Int], start: Int, end: Int): Int = {
    var sum = 0
    cfor(start)(_ < end, _ + 1) { i =>
      sum += array(i)
    }
    sum
  }

  def getNeurons = (blueNeurons ++ redNeurons).toList

  def attachToAgent(sBot: SBot){
    this.agent = Some(sBot)
  }

  def step() {
    agent map calcVision
  }
}
