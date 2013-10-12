package org.vastness.evo2dsim.neuro

import scala.collection.mutable.ArrayBuffer

class NeuronalNetwork {
  var synapses = ArrayBuffer[Synapse]()
  var neurons = ArrayBuffer[Neuron]()

  def addNeuron(n: Neuron){
    neurons += n
  }

  def addSynapse(id1: Int, id2: Int, weight:Double) {
    val n1 = neurons(id1)
    val n2 = neurons(id2)
    val s = new Synapse(n1, n2, weight)
    n1.addOutput(s)
    n2.addInput(s)
    synapses += s
  }

  def removeNeuron(id: Int) {
    val n = neurons.remove(id)
    n.inputSynapses.par.foreach((s: Synapse) => s.input.removeOutput(s))
    n.outputSynapses.par.foreach((s: Synapse) => s.output.removeInput(s))
    synapses --= n.inputSynapses ++ n.outputSynapses
  }

  def removeNeuron(n: Neuron) = removeNeuron(neurons.indexOf(n))

  def removeSynapse(id: Int) {
    val s = synapses.remove(id)
    s.input.removeOutput(s)
    s.output.removeInput(s)
  }

  def removeSynapse(s: Synapse) = removeSynapse(synapses.indexOf(s))

  def step() { //Order matters
    neurons.par.foreach(_.step())
    synapses.par.foreach(_.step())
  }
}
