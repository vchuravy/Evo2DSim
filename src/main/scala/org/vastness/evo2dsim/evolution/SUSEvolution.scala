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

package org.vastness.evo2dsim.evolution

import scala.annotation.tailrec
import scala.util.Random

/**
 * Implements stochastic universal sampling
 */
class SUSEvolution (val poolSize: Int)
  extends Evolution {

  override def nextGeneration(results: Genomes): Genomes = {
    val line = numberLine(normalizeResults(results.toSeq))
    assert(line.size == poolSize)

    def select(x: Double) = {
      line.filter(_._1 <= x).maxBy(_._1)._2
    }

    val stepSize = 1.0/poolSize
    val startingPoint = Random.nextDouble * stepSize
    //Generate a newId from 0 until poolSize
    var counter = -1
    def nextId(x: Double): Int = {
      counter += 1
      counter
    }

    ( for(x <- startingPoint to 1.0 by stepSize; id = select(x)) yield nextId(x) -> (0.0, results(id)._2.mutate) ).toMap
  }

  /**
   * Returns positive values for the fitness
   */
  def normalizeResults(results: Seq[(Int,(Double, Genome))]): List[(Int, Double)] = {
    val abs_rel = (results minBy {case (_, (fitness, _)) => fitness})._2._1 match {
      case x: Double if x <= 0 => (y: Double) => (y-x)+1 // So that we don't have a genome with zero fitness
      case _ => (y: Double) => y
    }

    val total = results.foldLeft(0.0){case (acc, (_, (fitness, _))) => acc + abs_rel(fitness)}
    def f_norm(x: Double) = abs_rel(x) / total

    ( for((key, (fitness, _)) <- results) yield key -> f_norm(fitness) ).sortBy(_._1).toList
  }

  def numberLine(results: List[(Int, Double)]) = _numberLine(0.0, results, List.empty[(Double, Int)]).reverse

  @tailrec
  private def _numberLine(nextIndex: Double, elems: List[(Int, Double)], acc: List[(Double, Int)]): List[(Double, Int)] = elems match {
    case Nil => acc
    case (key, fitness) :: xs => _numberLine(nextIndex + fitness, xs, (nextIndex, key) +: acc)
  }
}
