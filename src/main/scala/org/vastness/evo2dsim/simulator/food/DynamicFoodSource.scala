package org.vastness.evo2dsim.simulator.food

import org.vastness.evo2dsim.gui.Color


/**
 * Implements a dynamic food source where the reward is depending on the number of individuals around it.
 * @see FoodSource
 * @param constant reward bias
 */
class DynamicFoodSource(color: Color, max: Int,  constant: Double) extends FoodSource(color, max) {

  override def reward() = feeders.size - constant
}
