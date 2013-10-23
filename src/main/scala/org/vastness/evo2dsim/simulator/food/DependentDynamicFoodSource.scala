package org.vastness.evo2dsim.simulator.food

/**
 * Implements a dynamic food source, where the reward is dependent on an other food source
 * this.n - other.n - constant
 * @see DynamicFoodSource
 * @param other the other food source
 */
class DependentDynamicFoodSource(color: Int, max: Int, constant: Double, other: FoodSource) extends DynamicFoodSource(color, max, constant) {

  override def reward() = super.reward() - other.feeders.size
}
