package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import scala.collection.mutable

/**
 * Implements a binary genome in the following format
 * Biases of Neuron + Weights
 * @param nn
 */
class BinaryGenome(nn: NeuronalNetwork) extends Genome(nn) {
  private val (currentID, neurons, synapses) = nn.serializeNetwork()

  def toSerializedNN:
  (Int,
    mutable.Traversable[(Int, Double, (Double) => Double)],
    mutable.Traversable[(Int, Int, Double)]) = (currentID, neurons, synapses)
}
