package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork

/**
 * Implements a binary genome in the following format
 * Biases of Neuron + Weights
 * @param nn
 */
class BinaryGenome(nn: NeuronalNetwork) extends Genome(nn) {
    def toArray = Array.fill[Double](0)(0)
}
