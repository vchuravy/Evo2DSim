package org.vastness.evo2dsim.evolution

abstract class Genome {
  def toSerializedNN:
  (Int,
    Iterable[(Int, Double, (Double) => Double)],
    Iterable[(Int,Int,Double)])

  def mutate: Genome
  def crossover(g: Genome): Genome

}
