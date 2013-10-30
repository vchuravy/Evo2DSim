package org.vastness.evo2dsim.evolution

import scala.collection.immutable


abstract class Genome {
  def toSerializedNN:
  (Int,
    immutable.Iterable[(Int, Double, (Double) => Double)],
    immutable.Iterable[(Int,Int,Double)])

  def mutateLocal(): Unit
  def mutate: Genome
  def crossover(g: Genome): Genome

}
