package org.vastness.evo2dsim.environment

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.simulator.food.StaticFoodSource
import org.vastness.evo2dsim.gui.Color

class BasicRandomEnvironment(timeStep:Int, steps:Int) extends BasicEnvironment(timeStep, steps) {
  override protected def addFoodSources(edges: Seq[Vec2]) {
    val f1 = new StaticFoodSource(color = Color.RED, max = 8, reward = 1)
    val f2 = new StaticFoodSource(color = Color.RED, max = 8, reward = -0.3)

    sim.addFoodSource(newRandomPosition, radius = 0.1f, activationRange = 0.15f, f1)
    sim.addFoodSource(newRandomPosition, radius = 0.1f, activationRange = 0.5f, f2)
  }

}
