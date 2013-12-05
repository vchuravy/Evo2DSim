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

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.simulator.{Simulator, Agent}
import org.vastness.evo2dsim.simulator.light.{LightCategory, LightSource}
import org.vastness.evo2dsim.gui.Color

/**
 * Implements an S-Bot agent similar to the enki simulator.
 *   val radius = 0.06f  S-Bot size 6cm
 *   val mass = 0.66f S-Bot weight 660g
 */
class SBot(id: Int, pos: Vec2, sim: Simulator)
  extends Agent(id, pos, sim, radius = 0.06f, mass = 0.66f) {
  val light = new LightSource(Color.BLUE, this, LightCategory.AgentLight)
  sim.lightManager.addLight(light)

  override def color = light.color

}
