package org.vastness.evo2dsim.neuro


class Synapse(i: Neuron, o:Neuron, w: Double){
  val input = i
  val output = o
  var weight = w
  var value = input.calcOutput * weight
  def step() {
    value = input.calcOutput * weight
  }
}
