package org.vastness.evo2dsim.neuro

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable

class NeuronalNetwork {
  var synapses = ArrayBuffer[Synapse]()
  var neurons = mutable.HashMap[Int, Neuron]()

  private var currentID = -1
  def nextID:Int = {
    currentID += 1
    currentID
  }

  def addNeuron(n: Neuron){
    val id = nextID
    n.id = id
    neurons += ((id,n))
  }

  def addNeurons(ns: Traversable[Neuron]){
    val nsHash =
      for(n <- ns ;val id = nextID) yield (id,n)
    neurons ++= nsHash
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
    neurons.remove(id).get match {
      case n: Neuron => {
        n.inputSynapses.par.foreach((s: Synapse) => s.input.removeOutput(s))
        n.outputSynapses.par.foreach((s: Synapse) => s.output.removeInput(s))
        synapses --= n.inputSynapses ++ n.outputSynapses
      }
    }
  }

  def removeSynapse(id: Int) {
    val s = synapses.remove(id)
    s.input.removeOutput(s)
    s.output.removeInput(s)
  }

  def removeSynapse(s: Synapse) {
    removeSynapse(synapses.indexOf(s))
  }

  def step() { //Order matters
    neurons.par.foreach {case (_, n) => n.step()}
    synapses.par.foreach {_.step()}
  }

  /**
   * Use this function to generate a linear network between inputs and outputs
   * We iterate over the inputs and then over the outputs.
   * @param inputs
   * @param outputs
   * @param weights must have size == inputs.size * outputs.size
   */
  def generateLinearNetwork(inputs: Seq[Neuron], outputs: Seq[Neuron], weights: Seq[Double]){
    assert((inputs ++ outputs).diff(neurons.values.toSeq).isEmpty, "There are some neurons which are not part of this network")
    assert(weights.size == inputs.size*outputs.size, "The number of weights is off")

    val tempSynapses = new Array[Synapse](inputs.size*outputs.size)
    var c = 0
    for(input: Neuron <- inputs){
      for(output: Neuron <- outputs){
        val s = new Synapse(input, output, weights(c))
        input.addOutput(s)
        output.addInput(s)
        tempSynapses(c) = s
        c += 1
      }
    }
    synapses ++= tempSynapses
  }
}
