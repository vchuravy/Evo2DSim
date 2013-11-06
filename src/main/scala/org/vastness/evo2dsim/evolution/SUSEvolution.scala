package org.vastness.evo2dsim.evolution

import scala.annotation.tailrec
import scala.util.Random

/**
 * Implements stochastic universal sampling
 */
class SUSEvolution (poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, evaluationPerGeneration: Int, timeStep: Int)
  extends Evolution(poolSize, groupSize, evaluationSteps, generations, evaluationPerGeneration, timeStep) {

  override def nextGeneration(results: Seq[(Int, (Double, Genome))]): Map[Int, (Double, Genome)] = {
    val line = numberLine(normalizeResults(results))
    assert(line.size == results.size)

    def select(x: Double) = {
      line.filter(_._1 <= x).maxBy(_._1)._2
    }

    val rMap = results.toMap
    val stepSize = 1.0/line.size
    val startingPoint = Random.nextDouble * stepSize

    ( for(x <- startingPoint to 1.0 by stepSize; id = select(x)) yield id -> (0.0, rMap(id)._2.mutate) ).toMap
  }

  /**
   * Returns positive values for the fitness
   */
  def normalizeResults(results: Seq[(Int,(Double, Genome))]): Map[Int, Double] = {
    val abs_rel = (results minBy {case (_, (fitness, _)) => fitness})._2._1 match {
      case x: Double if x <= 0 => (y: Double) => (y-x)+1 // So that we don't have a genome with zero fitness
      case _ => (y: Double) => y
    }

    val total = results.foldLeft(0.0){case (acc, (_, (fitness, _))) => acc + abs_rel(fitness)}
    def f_norm(x: Double) = abs_rel(x) / total

    ( for((key, (fitness, _)) <- results) yield key -> f_norm(fitness) ).toMap
  }

  def numberLine(results: Map[Int, Double]) = _numberLine(0.0, results.toList, Seq.empty[(Double, Int)])

  @tailrec
  private def _numberLine(nextIndex: Double, elems: List[(Int, Double)], acc: Seq[(Double, Int)]): Seq[(Double, Int)] = elems match {
    case Nil => acc
    case (key, fitness) :: xs => _numberLine(nextIndex + fitness, xs, (nextIndex, key) +: acc)
  }
}
