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
    addNeuron(id,n)
  }

  private def addNeuron(id: Int, n:Neuron){
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
   * Serializes synapses
   * @return (From.ID,To.ID,Weight)
   */
  private def serializeSynapses: mutable.Traversable[(Int,Int,Double)] =
    for (s <- synapses) yield (s.input.id,s.output.id,s.weight)

  /**
   * Serializes neurons
   * @return (ID, bias)
   */
  private def serializeNeurons: mutable.Traversable[(Int, Double, (Double) => Double)] =
    for ((nID, n) <- neurons) yield (nID, n.bias, n.t_func)

  def serializeNetwork(){
    (currentID, serializeNeurons, serializeSynapses)
  }

  private def initializeSynapses(synapses: mutable.Traversable[(Int,Int,Double)]){
    for((id1,id2,weight) <- synapses.par) addSynapse(id1,id2,weight)
  }

  /**
   * Initialize neurons
   * WARNING: Sensors and motors have to be initialized
   */
  private def initializeNeurons(neurons: mutable.Traversable[(Int, Double, (Double) => Double)] ){
    if(currentID == -1) println("Warning: It might be that you forgot to initialize motors and sensors.")
    for((id, bias, t_func) <- neurons){
      if(this.neurons.contains(id)) {
        val n =  this.neurons(id)
        n.bias = bias
        n.t_func = t_func
      } else {
       addNeuron(id, new Neuron(bias, t_func))
      }
    }
  }

  /**
   * Initializes neurons and synapses. Warning: Motors and sensors have to be initialized first.
   * @param currentID
   * @param neurons
   * @param synapses
   */
  def initializeNetwork(currentID: Int,
                        neurons: mutable.Traversable[(Int, Double, (Double) => Double)],
                        synapses: mutable.Traversable[(Int,Int,Double)]) {
    initializeNeurons(neurons)
    initializeSynapses(synapses)
    this.currentID = currentID
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