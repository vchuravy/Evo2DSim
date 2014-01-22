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

package org.vastness.evo2dsim.core.simulator.food

import org.vastness.evo2dsim.core.simulator.{Simulator, Entity, Agent}
import org.vastness.evo2dsim.core.simulator.light.{LightCategory, LightSource}
import org.vastness.evo2dsim.core.gui.Color

/**
 * Base class for food sources. Gives reward to the first n individuals
 * @param c on which color channel the light source is sending
 * @param max maximal numbers of individuals who can feed from this source
 */
abstract class FoodSource(c: Color, var max: Int, var radius: Float, var activationRange: Float, var smellRange: Float) {
  require(radius > 0, "Radius must be bigger than zero")
  require(radius <= activationRange, "ActivationRange can't be smaller than radius")
  require(c == Color.BLUE || c == Color.RED, "Only Blue or Red is supported")
  require(smellRange >= activationRange, "SmellRange can't be smaller than activationRange")

  protected var light: Option[LightSource] = None

  def initialize(e: Entity, sim: Simulator) {
    val l = new LightSource(c, e, LightCategory.FoodSourceLight, radius)
    l.active_ = true
    sim.lightManager.addLight(l)
    light = Some(l)
  }

  def color = light match{
    case Some(l) => l.color
    case None => Color.CYAN // Not initialized
  }

  var feeders = Set.empty[Agent]
  var listeners = Set.empty[Agent]

  def reward: Double

  def step(){
    val r = reward
    feeders foreach (_.fitness += r)
    listeners foreach (_.currentReward = r)
  }
}
