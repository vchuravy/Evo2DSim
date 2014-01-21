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
import org.vastness.evo2dsim.core.simulator.Simulator.Flags

trait DefaultSettings extends Settings{
  override val origin = new Vec2(1.515f,1.515f)
  override val halfSize = 1.5f
  override def spawnSize  = halfSize*0.8f

  override val foodRadius: Float = 0.17f
  override def foodOffset: Float = 2f*foodRadius
  override def activationRange: Float = foodRadius * 1.3f
  override def smellRange: Float = activationRange * 1.3f
  override def artificialSmellMemory: Boolean = false
  override def simFlags = Flags()
  override def agentLimit = Int.MaxValue
}
