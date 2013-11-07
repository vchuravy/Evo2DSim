package org.vastness.evo2dsim.simulator

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import org.vastness.evo2dsim.evolution.Genome

abstract class Controller() {
  val nn = new NeuronalNetwork()

  def toGenome: Genome

  def fromGenome(genome: Genome): Unit

  def attachToAgent(agent: Agent): Unit

  def initialize(weights: Array[Double])
  def initializeRandom(random: () => Double)
  def initializeZeros()

  def sensorStep()

  def controllerStep(){
    nn.step()
  }

  def motorStep()

}
