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

package org.vastness.evo2dsim.core.environment.mixins.settings

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.core.evolution.Evolution.Generation
import org.vastness.evo2dsim.core.simulator.AgentID

trait TestSettings extends DefaultSettings {
  override def spawnSize  = 0.0f

  // Positions agents 50cm around the origin.
  override def newRandomPosition: Vec2 = {
    val dir = new Vec2(randomFloat, randomFloat)
    dir.normalize()
    dir mul 0.5f
  }

  override val foodRadius: Float = 0.06f
  override def foodOffset: Float = 0f
  override def activationRange: Float = 0f
  override def smellRange: Float = 0f
  override def artificialSmellMemory: Boolean = false

  var agent_pos: Map[AgentID, Vec2] = Map.empty

  override def initializeAgents(genomes: Generation) = {
    super.initializeAgents(genomes)
    agent_pos = agents map { case (id, a) => id -> a.position}
    agents
  }
}
