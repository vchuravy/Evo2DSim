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
trait  Environment {
  def timeStep: Int
  def steps:Int

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

  private def updateSimulation() {
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
      updateSimulation()
    }
  }

  def initializeStatic()
  def initializeAgents(genomes: Map[Int, (Double, Genome)])
}
