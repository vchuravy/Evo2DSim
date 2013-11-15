package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.TransferFunction

trait Genome {
  def toSerializedNN:
  (Int,
    Iterable[(Int, Double, TransferFunction)],
    Iterable[(Int,Int,Double)])

  def mutate: Genome
  def crossover(g: Genome): Genome
  def history: List[Int]
  def addId(id: Int)
}
