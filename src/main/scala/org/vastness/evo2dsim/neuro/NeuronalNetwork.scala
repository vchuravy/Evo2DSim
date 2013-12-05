/*
 * This file is part of Evo2DSim.
 *
 * Evo2DSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vastness.evo2dsim.neuro

import scala.collection.mutable.ArrayBuffer

class NeuronalNetwork {
  var synapses = ArrayBuffer[Synapse]()
  var neurons = Map[Int, Neuron]()

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
    neurons(id) match {
      case n: Neuron => {
        n.inputSynapses.par.foreach((s: Synapse) => s.input.removeOutput(s))
        n.outputSynapses.par.foreach((s: Synapse) => s.output.removeInput(s))
        synapses --= n.inputSynapses ++ n.outputSynapses
      }
    }
    neurons -= id
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
  private def serializeSynapses: Iterable[(Int,Int,Double)] =
    ( for (s <- synapses) yield (s.input.id,s.output.id,s.weight) ).to[Iterable]

  /**
   * Serializes neurons
   * @return (ID, bias)
   */
  private def serializeNeurons: Iterable[(Int, Double, TransferFunction)] =
    ( for ((nID, n) <- neurons) yield (nID, n.bias, n.t_func) ).to[Iterable]

  def serializeNetwork() =
    (currentID, serializeNeurons, serializeSynapses)

  private def initializeSynapses(synapses: Iterable[(Int,Int,Double)]){
    synapses.foreach(elem => addSynapse(elem._1,elem._2,elem._3))
  }

  /**
   * Initialize neurons
   * WARNING: Sensors and motors have to be initialized
   */
  private def initializeNeurons(neurons: Iterable[(Int, Double, TransferFunction)] ){
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
   */
  def initializeNetwork(currentID: Int,
                        neurons: Iterable[(Int, Double, TransferFunction)],
                        synapses: Iterable[(Int,Int,Double)]) {
    initializeNeurons(neurons)
    initializeSynapses(synapses)
    this.currentID = currentID
  }

  /**
   * Use this function to generate a linear network between inputs and outputs
   * We iterate over the inputs and then over the outputs.
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