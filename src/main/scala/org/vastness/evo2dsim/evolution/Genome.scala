package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import scala.collection.immutable


abstract class Genome(nn: NeuronalNetwork) {
  def toSerializedNN:
  (Int,
    immutable.Iterable[(Int, Double, (Double) => Double)],
    immutable.Iterable[(Int,Int,Double)])

  def mutate(): Genome
  def crossover(g: Genome): Genome

}
