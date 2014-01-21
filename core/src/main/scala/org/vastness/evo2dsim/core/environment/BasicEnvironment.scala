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

package org.vastness.evo2dsim.core.environment

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.core.simulator.{Simulator, Agent}
import scala.collection.Map
import org.vastness.evo2dsim.core.simulator.food.FoodSource
import scala.annotation.tailrec
import org.vastness.evo2dsim.core.evolution.genomes.Genome
import org.vastness.evo2dsim.core.evolution.Evolution.Generation

/**
 * @see Environment
 */
abstract class BasicEnvironment(timeStep:Int, steps:Int) extends Environment(timeStep, steps) {
  // Overwritten in  mixins.settings
  def foodRadius: Float
  def foodOffset: Float
  def activationRange: Float
  def smellRange: Float
  def artificialSmellMemory: Boolean
  def simFlags: Simulator.Flags

  // Overwritten in mixins.foodSources
  protected def foodSources: List[FoodSource]

  // Overwritten in mixins.foodPos
  protected def foodPos: List[Vec2]

  def sizes = Array[Vec2](new Vec2(-halfSize,-halfSize), new Vec2(-halfSize,halfSize), new Vec2(halfSize,halfSize), new Vec2(halfSize,-halfSize))
  def edges = for(i <- 0 until sizes.length) yield origin add sizes(i)

  def text: String = "" + stepCounter
  def initializeStatic() {
    sim.createWorldBoundary(edges.toArray, text)

    //setFlags
    sim.flags = simFlags

    @tailrec
    def addFood(food: List[FoodSource], pos: List[Vec2]) {
      (food, pos) match {
        case (f :: fs, _p :: ps) =>
          sim.addFoodSource(_p,f)
          addFood(fs, ps)
        case (Nil, Nil) =>
        case (fs, Nil) => throw new Exception(s"There are still some Food Source left: $fs")
        case (Nil, ps) => println("Warning two many positions")
      }
    }
    addFood(foodSources, foodPos)
  }

  protected def normToOrigin(p: Vec2): Vec2 = {
      val v = p sub origin
      v.normalize()
      v
  }

  protected def edgeLocations = edges map {e => e sub (normToOrigin(e) mul foodOffset)}

  def initializeAgents(genomes: Generation){
    def pos = newRandomPosition
    def angle = sim.random.nextFloat()
    def addWithGenome(a: Agent, g: Genome): Agent = {
      a.controller.init(g)
      if(artificialSmellMemory) a.activateArtificialSmellMemory()
      a
    }

    agents = ( for( (id,(_, genome)) <- genomes) yield
      (id, addWithGenome(sim.addAgent(pos, angle, sim.Agents.SBot, id), genome))
      ).toMap
  }
}
