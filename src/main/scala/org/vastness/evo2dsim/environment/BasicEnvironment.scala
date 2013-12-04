package org.vastness.evo2dsim.environment

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.simulator.food.StaticFoodSource
import org.vastness.evo2dsim.gui.Color
import org.vastness.evo2dsim.simulator.Agent
import org.vastness.evo2dsim.evolution.Genome
import scala.collection.Map

/**
 * @see Environment
 */
class BasicEnvironment(val timeStep:Int, val steps:Int) extends Environment{

  val origin = new Vec2(1.515f,1.515f)
  val halfSize = 1.5f

  val fRadius: Float = 0.17f
  val aRange: Float = fRadius * 1.3f

  val sizes = Array[Vec2](new Vec2(-halfSize,-halfSize), new Vec2(-halfSize,halfSize), new Vec2(halfSize,halfSize), new Vec2(halfSize,-halfSize))
  val edges = for(i <- 0 until sizes.length) yield origin add sizes(i)

  val f1 = new StaticFoodSource(color = Color.RED, max = 8, reward = 1)
  val f2 = new StaticFoodSource(color = Color.RED, max = 8, reward = -1)

  def initializeStatic() {
    sim.createWorldBoundary(edges.toArray)
    addFoodSources(edges)
  }

  protected def normToOrigin(p: Vec2): Vec2 = {
      val v = p sub origin
      v.normalize()
      v
  }

  protected def foodPos = edges map {e => e sub (normToOrigin(e) mul 2f*fRadius)}

  protected def addFoodSources(edges: Seq[Vec2]) {
    sim.addFoodSource(foodPos(0), radius = fRadius, activationRange = aRange, f1)
    sim.addFoodSource(foodPos(2), radius = fRadius, activationRange = aRange, f2)
  }

  def initializeAgents(genomes: Map[Int, (Double, Genome)]){
    def pos = newRandomPosition
    def addWithGenome(id: Int, a: Agent, g: Genome): Agent = {
      g.addId(id)
      a.controller.get.fromGenome(g)
      a
    }

    agents = ( for( (id,(_, genome)) <- genomes) yield
      (id, addWithGenome(id, sim.addAgent(pos, sim.Agents.SBotControllerLinear), genome))
      ).toMap
  }
}
