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

package org.vastness.evo2dsim.core.environment.mixins.foodSources

import org.vastness.evo2dsim.core.gui.Color
import org.vastness.evo2dsim.core.simulator.food.StaticFoodSource

trait StaticFoodSources extends FoodSources {
  protected val food = new StaticFoodSource(color = Color.RED, max = 8, reward = 1, foodRadius, activationRange, smellRange)
  protected val poison =  new StaticFoodSource(color = Color.RED, max = 8, reward = -1, foodRadius, activationRange, smellRange)
  def foodSources = List(food, poison)
  override def signallingStrategy = {
    // Calculate signal to time ratio. Both can be NaN iff timeNear == 0.0
    val foodSignal = if(food.timeNear == 0) 0.0 else food.signalNear.toDouble / food.timeNear
    val poisonSignal = if(poison.timeNear == 0) 0.0 else poison.signalNear.toDouble / poison.timeNear
    
    val s = foodSignal - poisonSignal
    Some(s / agents.size)
  }
}
