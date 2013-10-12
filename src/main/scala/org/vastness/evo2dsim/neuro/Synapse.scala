package org.vastness.evo2dsim.neuro


class Synapse(i: Neuron, o:Neuron){
  val input = i
  val output = o
  var value = input.calcOutput
  def step() {
    value = input.calcOutput
  }
}
