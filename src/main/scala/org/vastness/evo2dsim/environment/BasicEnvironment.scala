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
class BasicEnvironment(timeStep:Int, steps:Int) extends Environment(timeStep, steps){

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
