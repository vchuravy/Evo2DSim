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
import breeze.linalg.DenseMatrix
import org.vastness.evo2dsim.utils.LinearMapping
import breeze.generic.URFunc
import spire.implicits._
import spire.math._
import scala.Some

class SBotLightSensor(segments: Int, bias: Double) extends LinearMapping {
  val fov = 360
  val resolution = fov
  var debug = false
  private var agent: Option[SBot] = None
  private val redNeurons = new Array[Neuron](segments)
  private val blueNeurons = new Array[Neuron](segments)

  assert(resolution%segments == 0)
  val pixels = resolution/segments

  val UPPER_OUTPUT_LIMIT = Rational(4.0)
  val LOWER_OUTPUT_LIMIT = Rational(-4.0)
  val UPPER_INPUT_LIMIT = Rational(pixels)
  val LOWER_INPUT_LIMIT = Rational(0.0)

  createNeurons()

  @inline
  def visionStrip: PartialFunction[Color, DenseMatrix[Int]] = {
    case c: Color if c2Idx.isDefinedAt(c) => visionStorage(c2Idx(c), ::)
  }

  @inline
  def c2Idx: PartialFunction[Color, Int] = {
    case Color.RED => 0
    case Color.BLUE => 1
  }

  def getVisionStrip: Map[Color, DenseMatrix[Int]] = ( Seq(Color.RED, Color.BLUE) collect
    {case c: Color if visionStrip.isDefinedAt(c) => c -> visionStrip(c)} ).toMap

  private val visionStorage = DenseMatrix.zeros[Int](2,resolution)

  @inline
  def clear() {
    visionStorage := 0
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
        val lightPosition = sBot.body.getLocalPoint(light.position)
        val distance = lightPosition.normalize() - radius


        val aperture    = FastMath.atan(radius / distance)
        val bearingRad  = FastMath.atan2(lightPosition.x, lightPosition.y) // clockwise angle
        val beginAngle  = FastMath.toDegrees(bearingRad - aperture)
        val endAngle    = FastMath.toDegrees(bearingRad + aperture)

        // Calculation taken from Enki
        val start: Int = FastMath.floor((resolution - 1) * 0.5 * (beginAngle / fov + 1)).toInt
        val end: Int = FastMath.ceil((resolution - 1) * 0.5 * (endAngle / fov + 1)).toInt

        visionStorage(c2Idx(light.color), start to end) := 1 //TODO: fog, noise, objects standing in sight?
      }
    }
  }

  private def createNeurons() {
    for( i <- 0 until segments){
      blueNeurons(i) = new SensorNeuron(bias, TransferFunction.SIG, getAverageFunc(Color.BLUE, i) )
      redNeurons(i) = new SensorNeuron(bias, TransferFunction.SIG, getAverageFunc(Color.RED, i))
    }
  }

  @inline
  def getAverageFunc(c: Color, index: Int): () => Rational = {
    () => {
      transform(sum(visionStorage(c2Idx(c),pixels * index until pixels * (index + 1))))
    }
  }

  /**
   * Breeze only has this sum function for doubles
   */
  private val sum:URFunc[Int, Int] = new URFunc[Int, Int] {
    def apply(cc: TraversableOnce[Int]) =  {
      cc.sum
    }
  }

  def getNeurons = (blueNeurons ++ redNeurons).toList

  def attachToAgent(sBot: SBot){
    this.agent = Some(sBot)
  }

  def step() {
    agent map calcVision
  }
}
