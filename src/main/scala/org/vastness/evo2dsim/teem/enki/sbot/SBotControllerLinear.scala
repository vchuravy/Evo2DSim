package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.evolution.{BinaryGenome, Genome}
import org.vastness.evo2dsim.App

class SBotControllerLinear(sBot: SBot ) extends SBotController(sBot) {

  override def toGenome:Genome = new BinaryGenome(nn,size)

  override def fromGenome(genome: Genome) = genome match {
    case g: BinaryGenome => {initialize(g.toArray)}
  }

  override def initialize(weights: Array[Double]) {
    nn.generateLinearNetwork(sensorNeurons, motorNeurons, weights)
  }

  override def initializeRandom(){
    val weights = Array.fill(size){sBot.sim.random.nextDouble()*2-1}
    initialize(weights)
  }

  override def initializeZeros(){
    val weights = Array.fill(size){0.0}
    initialize(weights)
  }
}
