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

package org.vastness.evo2dsim.core.evolution

import scala.annotation.tailrec
import scala.util.Random
import org.vastness.evo2dsim.core.evolution.genomes.Genome
import org.vastness.evo2dsim.core.evolution.Evolution.{Genomes, Generation}
import org.vastness.evo2dsim.core.simulator.AgentID

/**
 * Implements stochastic universal sampling
 */
class SUSEvolution (val config: EvolutionConfig)
  extends Evolution {

  /**
   * Implements a Stochastic Universal Sampling algorithm
   * TODO: Use crossover
   * @param generation the previous generation
   * @return the next generation
   */
  override def nextGeneration(idx: Int, generation: Generation): Generation = {
    val genomes = Evolution.extractGenomes(generation)
    val fitness = Evolution.extractFitness(generation)

    val line = numberLine(normalizeResults(fitness))
    assert(line.size == config.poolSize)

    def select(x: Double) = {
      line.filter(_._1 <= x).maxBy(_._1)._2
    }

    val stepSize = 1.0/config.poolSize
    val startingPoint = Random.nextDouble * stepSize
    //Generate a newId from 0 until poolSize
    var counter = -1
    def nextId(x: Double): Int = {
      counter += 1
      counter
    }

    val nextGenomes: Genomes = (
      for(x <- startingPoint to 1.0 by stepSize; id = select(x)) yield
        nextId(x) -> genomes(id).mutate ).toMap
    Evolution.groupGenomes(idx, nextGenomes, config)
  }

  /**
   * Normalizes the fitness values
   * @return A sequences of (id, fitness) sorted by fitness
   */
  def normalizeResults(results: Map[Int, Double]): Seq[(Int, Double)] = {
    val abs_rel = results.values.min  match {
      case x: Double if x <= 0 => (y: Double) => (y-x)+1 // So that we don't have a genome with zero fitness
      case _ => (y: Double) => y // If we only have positive fitness values return the identity function
    }

    val total = results.values.foldLeft(0.0)((acc, x) => acc + abs_rel(x))
    def f_norm(x: Double) = abs_rel(x) / total

    val norm_results = results map {
      case (id, fitness) => id -> f_norm(fitness)
    }

    norm_results.toSeq.sortBy(_._2)
  }

  def numberLine(results: Seq[(Int, Double)]) = _numberLine(0.0, results.toList, List.empty[(Double, Int)]).reverse

  @tailrec
  private def _numberLine(nextIndex: Double, elems: List[(Int, Double)], acc: List[(Double, Int)]): List[(Double, Int)] = elems match {
    case Nil => acc
    case (key, fitness) :: xs => _numberLine(nextIndex + fitness, xs, (nextIndex, key) +: acc)
  }
}
