package org.vastness.evo2dsim.environment

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.simulator.food.StaticFoodSource
import org.vastness.evo2dsim.gui.Color
import org.vastness.evo2dsim.simulator.Agent
import org.vastness.evo2dsim.evolution.Genome


/**
 * @see Environment
 */
class BasicEnvironment(timeStep:Int, simSpeed:Int, steps:Int) extends Environment(timeStep, simSpeed, steps){

  val origin = new Vec2(1.015f,1.015f)
  val halfSize = 1f
  var agents = IndexedSeq.empty[Agent]

  override def initializeStatic() {
    val sizes = Array[Vec2](new Vec2(-halfSize,-halfSize), new Vec2(-halfSize,halfSize), new Vec2(halfSize,halfSize), new Vec2(halfSize,-halfSize))
    val edges = for(i <- 0 until sizes.length) yield origin add sizes(i)

    sim.createWorldBoundary(edges.toArray)

    val f1 = new StaticFoodSource(color = Color.RED, max = 8, reward = 1)
    val f2 = new StaticFoodSource(color = Color.RED, max = 8, reward = -1)

    sim.addFoodSource(edges(0) add new Vec2(0.1f, 0.1f), radius = 0.1f, activationRange = 0.15f, f1)
    sim.addFoodSource(edges(2) add new Vec2(-0.1f, -0.1f), radius = 0.1f, activationRange = 0.5f, f2)
  }

  override def initializeAgents(populationSize: Int, genomes: List[Genome]){
    def pos = origin.add(new Vec2(sim.random.nextFloat()-0.5f, sim.random.nextFloat()-0.5f))
    def addWithGenome(a: Agent, g: Genome): Agent = {
      a.controller.get.fromGenome(g)
      a
    }

    agents = for( i <- 0 until populationSize) yield
      if (i < genomes.size) addWithGenome(sim.addAgent(pos, sim.Agents.SBotControllerLinear), genomes(i))
      else sim.addAgent(pos, sim.Agents.SBotControllerLinearRandom)
  }
}
