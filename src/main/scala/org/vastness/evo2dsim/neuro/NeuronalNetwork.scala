package org.vastness.evo2dsim.neuro

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.collection.immutable

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
    _addNeuron(id,n)
  }

  private def _addNeuron(id: Int, n:Neuron){
    n.id = id
    neurons += ((id,n))
  }

  def addNeurons(ns: Traversable[Neuron]){
    val nsHash =
      for(n <- ns ; id = nextID) yield {
        n.id = id
        (id,n)
      }
    neurons ++= nsHash
  }

  def addSynapse(id1: Int, id2: Int, weight:Double) {
    require(id1 != -1 && id2 != -1)
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
  private def serializeSynapses: immutable.Iterable[(Int,Int,Double)] =
    ( for (s <- synapses) yield (s.input.id,s.output.id,s.weight) ).to[immutable.Iterable]

  /**
   * Serializes neurons
   * @return (ID, bias)
   */
  private def serializeNeurons: immutable.Iterable[(Int, Double, (Double) => Double)] =
    ( for ((nID, n) <- neurons) yield (nID, n.bias, n.t_func) ).to[immutable.Iterable]

  def serializeNetwork() =
    (currentID, serializeNeurons, serializeSynapses)

  private def initializeSynapses(synapses: immutable.Iterable[(Int,Int,Double)]){
    synapses.foreach(elem => addSynapse(elem._1,elem._2,elem._3))
  }

  /**
   * Initialize neurons
   * WARNING: Sensors and motors have to be initialized
   */
  private def initializeNeurons(neurons: immutable.Iterable[(Int, Double, (Double) => Double)] ){
    if(currentID == -1) println("Warning: It might be that you forgot to initialize motors and sensors.")
    for((id, bias, t_func) <- neurons){
      if(this.neurons.contains(id)) {
        val n =  this.neurons(id)
        n.bias = bias
        n.t_func = t_func
      } else {
        assert(id != -1)
       _addNeuron(id, new Neuron(bias, t_func))
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
                        neurons: immutable.Iterable[(Int, Double, (Double) => Double)],
                        synapses: immutable.Iterable[(Int,Int,Double)]) {
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