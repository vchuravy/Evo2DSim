package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import scala.collection.mutable


abstract class Genome(nn: NeuronalNetwork) {
  def toSerializedNN:
  (Int,
    mutable.Traversable[(Int, Double, (Double) => Double)],
    mutable.Traversable[(Int,Int,Double)])

  def mutate()
  def crossover(g: Genome)

}
