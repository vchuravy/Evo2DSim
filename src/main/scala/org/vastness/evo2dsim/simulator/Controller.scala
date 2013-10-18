package org.vastness.evo2dsim.simulator

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import org.vastness.evo2dsim.evolution.Genome

abstract class Controller(agent: Agent) {
  val nn = new NeuronalNetwork()

  def toGenome: Genome

  def fromGenome(genome: Genome): Unit

  def initialize(weights: Array[Double])
  def initializeRandom()
  def initializeZeros()


  def step(){
    nn.step()
  }

}
