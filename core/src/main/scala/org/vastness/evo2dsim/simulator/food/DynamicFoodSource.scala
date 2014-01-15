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

package org.vastness.evo2dsim.simulator.food

import org.vastness.evo2dsim.gui.Color


/**
 * Implements a dynamic food source where the reward is depending on the number of individuals around it.
 * @see FoodSource
 * @param constant reward bias
 */
class DynamicFoodSource(color: Color,
                        max: Int,
                        constant: Double,
                        radius: Float,
                        activationRange: Float,
                        smellRange: Float) extends FoodSource(color, max, radius, activationRange, smellRange) {

  override def reward = feeders.size + constant
}
