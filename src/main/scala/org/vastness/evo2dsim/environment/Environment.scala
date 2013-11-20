package org.vastness.evo2dsim.environment

import org.vastness.evo2dsim.simulator.{Agent, Simulator}
import org.vastness.evo2dsim.evolution.Genome
import scala.concurrent.promise
import org.vastness.evo2dsim.gui.EnvironmentManager
import scala.collection.Map

/**
 * Implements the very basics for an environment
 * @param timeStep in ms
 * @param steps how many steps should the evaluation run?
 */
abstract class Environment(val timeStep: Int = 50, val steps:Int = 0) {
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
