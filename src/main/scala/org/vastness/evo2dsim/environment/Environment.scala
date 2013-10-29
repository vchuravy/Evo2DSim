package org.vastness.evo2dsim.environment

import org.vastness.evo2dsim.simulator.{Agent, Simulator}
import org.vastness.evo2dsim.evolution.Genome
import scala.concurrent.{Future, promise, future}
import org.vastness.evo2dsim.gui.EnvironmentManager

/**
 * Implements the very basics for an environment
 * @param timeStep in ms
 * @param steps how many steps should the evaluation run?
 */
abstract class Environment(val timeStep: Int = 50, val steps:Int = 0, val id: Int) {
  protected var stepCounter = 0
  val sim = new Simulator(new scala.util.Random().nextLong())
  var agents = IndexedSeq.empty[Agent]
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
  def initializeAgents(populationSize: Int, genomes: List[Genome])
}
