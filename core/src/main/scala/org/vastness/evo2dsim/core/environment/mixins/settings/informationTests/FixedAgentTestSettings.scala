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

package org.vastness.evo2dsim.core.environment.mixins.settings.informationTests

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.core.environment.mixins.settings.DefaultSettings
import org.vastness.evo2dsim.core.simulator.Simulator.Flags

trait FixedAgentTestSettings extends DefaultSettings with OneVarTest[Float] {
  val normVec = {
    val v = new Vec2(1f, 1f)
    v.normalize()
    v
  }

  def offSet: Float = foodRadius + 0.06f + varRange(varIdx).toFloat

  override def varRange = Range.Double.inclusive(0, 1, 0.1)
  override def spawnSize  = 0.0f

  // Positions agents at a fixed position with a fixed angle.
  override def newRandomPosition: Vec2 = normVec mul offSet
  override def newRandomAngle = 0f

  override def foodOffset: Float = 0f

  override def agentLimit: Int = 1

  override def simFlags = Flags(motors = false)
  override def varUpdate() {
    agents foreach { case (_, a) =>
      a.position = newRandomPosition
    }
  }
}
