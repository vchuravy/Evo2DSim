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

import org.vastness.evo2dsim.simulator.{Agent, Simulator}
import org.vastness.evo2dsim.evolution.Genome
import scala.concurrent.promise
import org.vastness.evo2dsim.gui.EnvironmentManager
import scala.collection.Map
import org.jbox2d.common.Vec2

/**
 * Implements the very basics for an environment
 */
abstract class Environment(val timeStep: Int, val steps: Int) {
  def origin: Vec2
  def halfSize: Float

  def spawnSize: Float = halfSize*0.8f

  def newRandomPosition: Vec2 = {
    def randomFloat: Float = (sim.random.nextFloat * 2) - 1
    origin add new Vec2(randomFloat * spawnSize, randomFloat * spawnSize)
  }

  protected var stepCounter = 0
  val sim = new Simulator(scala.util.Random.nextLong())
  var agents = Map.empty[Int, Agent]
  val p = promise[Environment]()

  var running = true
  var pause = false

  def updateSimulation() {
    sim.step(timeStep/1000.0f)
    stepCounter += 1
    if(steps == stepCounter) {
      running = false
      EnvironmentManager.remove(this)
      p success this
    }
  }

  def run(){
    while(running){
      if(!pause) updateSimulation()
    }
  }

  def initializeStatic()
  def initializeAgents(genomes: Map[Int, (Double, Genome)])
}
