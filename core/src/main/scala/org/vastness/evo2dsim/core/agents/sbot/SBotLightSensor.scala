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

package org.vastness.evo2dsim.core.agents.sbot

import org.vastness.evo2dsim.core.neuro._
import org.vastness.evo2dsim.core.simulator.light.LightSource
import org.vastness.evo2dsim.core.gui.Color
import org.apache.commons.math3.util.FastMath
import org.vastness.evo2dsim.core.utils.LinearMapping
import spire.implicits._

/**
 * Implements a two channel 360 Degree camera
 * @param segments In how many segments should we split up the Camera
 * @param bias default bias for connected neurons
 */
class SBotLightSensor(segments: Int, bias: Double) extends LinearMapping {
  val fov = 360
  require(fov <= 360, "A FOV higher then 360 Degree doesn't make any sense")
  val resolution = fov // If you need a higher resolution than 1 Grad = 1 Px

  private var agent: Option[SBot] = None
  private val redNeurons = new Array[SensorNeuron](segments)
  private val blueNeurons = new Array[SensorNeuron](segments)

  assert(resolution%segments == 0)
  val pixels = resolution/segments

  val UPPER_OUTPUT_LIMIT = 4.0
  val LOWER_OUTPUT_LIMIT = -4.0
  val UPPER_INPUT_LIMIT = pixels.toDouble
  val LOWER_INPUT_LIMIT = 0.0

  // Create the Neurons that are attached to this sensor
  createNeurons()

  /**
   * Returns the visionStrip belong to the color, think of this as a manual map
   * @return
   */
  @inline def visionStrip: PartialFunction[Color, Array[Int]] = {
    case Color.RED => red
    case Color.BLUE => blue
  }

  // After each step we store the results in these arrays
  private var red:  Array[Int] = Array.empty
  private var blue: Array[Int] = Array.empty

  /**
   * @see visionStrip a manual map the gives you the correct sensorValues for a given color.
   * @return
   */
  @inline def sensorValue: PartialFunction[Color, Array[NumberT]] = {
    case Color.RED => redSensorValues
    case Color.BLUE => blueSensorValues
  }

  // We store the sensorValue for each color.
  private var redSensorValues = new Array[NumberT](segments)
  private var blueSensorValues = new Array[NumberT](segments)

  /**
   *
   * @return the VisionStrip as a real Map
   */
  def getVisionStrip: Map[Color, Array[Int]] = ( Seq(Color.RED, Color.BLUE) collect
    {case c: Color if visionStrip.isDefinedAt(c) => c -> visionStrip(c)} ).toMap

  /**
   * Optimized function to fill the visionStrip
   * @param data to work upon
   * @param s where to start
   * @param e where to end
   * @return
   */
  @inline private def fillStrip(data :Array[Int])(s:Int, e:Int) {
    cfor(s)(_ <= e, _ + 1) { i =>
      data(i) = 1
    }
  }

  private val max = resolution - 1

  /**
   * Safely fill the vision Strip an take underfill and overfill into account
   * @param f The function to work with
   * @param s start
   * @param e end
   * @return
   */
  private def safeFillStrip(f: (Int, Int) => Unit)(s: Int, e: Int) {
    var start = s
    var end = e
    val negativeStart = if(start < 0) {
      start += max
      true
    } else false

    val endExceedingMax = if(end > max) {
      end -= resolution
      true
    } else false

    if(negativeStart && !endExceedingMax){
      f(0, end)
      f(start, max)
    } else if (endExceedingMax && !negativeStart) {
      f(start, max)
      f(0, end)
    } else if (negativeStart && endExceedingMax){
      f(0, max)
      println("WARNING: We just filled the whole strip, probably because we got weird input.")
    } else {
      f(start, end)
    }
  }

  /**
   * Fills visionStrip, if light from a source falls onto the area.
   * A point light source (in the center of the object that emits the light) shines light on the surface of an object
   * based upon the distance and relative position to the target.
   * The code is inspired by the enki
   */
  def calcVision(sBot: SBot) {
    // Temp storage
    val redT: Array[Int]   = new Array(resolution)
    val blueT: Array[Int]  = new Array(resolution)

    //Tmp access function
    @inline val visionStripT: PartialFunction[Color, Array[Int]] = {
      case Color.RED => redT
      case Color.BLUE => blueT
    }

    // Fill function to work on the temp data
    @inline def fillStripT(c: Color) = fillStrip(visionStripT(c)) _

    for(light: LightSource <- sBot.sim.lightManager.lightSources){
      if (light.active && sBot.light != light && light.radius > 0){
        val radius = light.radius
        val lightPosition = sBot.body.getLocalPoint(light.position)
        val distance = lightPosition.normalize() - radius // Subtract the light radius, because the SBots have the light strip around their body

        val aperture    = FastMath.atan(radius / distance)
        val bearingRad  = FastMath.atan2(lightPosition.x, lightPosition.y) // clockwise angle
        val beginAngle  = FastMath.toDegrees(bearingRad - aperture)
        val endAngle    = FastMath.toDegrees(bearingRad + aperture)

        val start:Int = FastMath.floor((resolution - 1) * ( (beginAngle+180) / fov )).toInt
        val end: Int = FastMath.ceil((resolution - 1) * ((endAngle+180) / fov )).toInt

        safeFillStrip(fillStripT(light.color))(start, end)
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
      blueNeurons(i) = new SensorNeuron(-1, bias, TransferFunction.SIG, s"VisionBlue$i")(getSensorFunc(Color.BLUE, i))
      redNeurons(i) = new SensorNeuron(-1, bias, TransferFunction.SIG, s"VisionRed$i")(getSensorFunc(Color.RED, i))
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

  def getNeurons = (blueNeurons ++ redNeurons).toSet

  def attachToAgent(sBot: SBot){
    this.agent = Some(sBot)
  }

  def step() {
    agent map calcVision
  }
}
