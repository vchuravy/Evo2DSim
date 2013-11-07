package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.evolution.{BinaryGenome, Genome}

class SBotControllerLinear() extends SBotController() {

  override def toGenome:Genome = BinaryGenome.initialize(nn, mutateBiases = false)

  override def initialize(weights: Array[Double]) {
    nn.generateLinearNetwork(sensorNeurons, motorNeurons, weights)
  }

  override def initializeRandom(random: () => Double){
    val weights = Array.fill(size){random() *2-1}
    initialize(weights)
  }

  override def initializeZeros(){
    val weights = Array.fill(size){0.0}
    initialize(weights)
  }
}
