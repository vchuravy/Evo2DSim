package org.vastness.evo2dsim.neuro


class Synapse(val input: Neuron, val output:Neuron, var weight: Double){
  var value = input.calcOutput * weight
  def step() {
    value = input.calcOutput * weight
  }

  override def toString = "Synapse from " + input + " to " + output + " weight: " + weight
}
