package org.vastness.evo2dsim.simulator.food

import org.vastness.evo2dsim.gui.Color

class StaticFoodSource(color: Color, max: Int, override val reward: Double) extends FoodSource(color,max) {
}
