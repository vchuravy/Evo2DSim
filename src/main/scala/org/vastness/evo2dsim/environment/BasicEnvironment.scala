package org.vastness.evo2dsim.environment

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.simulator.food.StaticFoodSource
import org.vastness.evo2dsim.gui.Color


/**
 * Implements the very basics for an environment
 * @param timeStep in ms
 * @param simSpeed relative to real time
 */
class BasicEnvironment(timeStep:Int, simSpeed:Int ) extends Environment(timeStep, simSpeed){

  val origin = new Vec2(1.015f,1.015f)
  val halfSize = 1f

  override def initialize() {
    val sizes = Array[Vec2](new Vec2(-halfSize,-halfSize), new Vec2(-halfSize,halfSize), new Vec2(halfSize,halfSize), new Vec2(halfSize,-halfSize))
    val edges = for(i <- 0 until sizes.length) yield origin add sizes(i)

    sim.createWorldBoundary(edges.toArray)

    val f1 = new StaticFoodSource(color = Color.RED, max = 8, reward = 1)
    val f2 = new StaticFoodSource(color = Color.RED, max = 8, reward = -1)

    sim.addFoodSource(edges(0) add new Vec2(0.1f, 0.1f), radius = 0.1f, activationRange = 0.15f, f1)
    sim.addFoodSource(edges(2) add new Vec2(-0.1f, -0.1f), radius = 0.1f, activationRange = 0.5f, f2)


    val agents = for( i <- 0 until 10) yield
      sim.addAgent(origin.add(new Vec2(sim.random.nextFloat()-0.5f, sim.random.nextFloat()-0.5f)), sim.Agents.SBotControllerLinearRandom)
  }
}
