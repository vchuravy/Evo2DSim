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

package org.vastness.evo2dsim.environment

import org.jbox2d.common.Vec2

class BasicRandomEnvironment(timeStep:Int, steps:Int) extends BasicEnvironment(timeStep, steps) {
  override protected def addFoodSources(edges: Seq[Vec2]) {
    val randomFoodPos = sim.random.shuffle(foodPos)
    sim.addFoodSource(randomFoodPos(0), radius = fRadius, activationRange = aRange, f1)
    sim.addFoodSource(randomFoodPos(1), radius = fRadius, activationRange = aRange, f2)
  }

}
