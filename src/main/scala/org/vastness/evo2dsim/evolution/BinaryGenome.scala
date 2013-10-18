package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork


class BinaryGenome(nn: NeuronalNetwork, val size: Int) extends Genome(nn) {
    def toArray = Array.fill[Double](size)(0)
}
