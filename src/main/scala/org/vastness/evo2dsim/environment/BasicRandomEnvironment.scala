package org.vastness.evo2dsim.environment

import org.jbox2d.common.Vec2

class BasicRandomEnvironment(timeStep:Int, steps:Int) extends BasicEnvironment(timeStep, steps) {
  override protected def addFoodSources(edges: Seq[Vec2]) {
    val randomFoodPos = sim.random.shuffle(foodPos)
    sim.addFoodSource(randomFoodPos(0), radius = fRadius, activationRange = aRange, f1)
    sim.addFoodSource(randomFoodPos(1), radius = fRadius, activationRange = aRange, f2)
  }

}
