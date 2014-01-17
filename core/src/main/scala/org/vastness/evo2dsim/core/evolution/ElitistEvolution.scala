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

import scala.util.Random
import org.vastness.evo2dsim.core.evolution.Evolution.Generation

/**
 * Implements a Elitist Evolution algorithm
 * TODO: Use crossover
 * @param percent
 * @param config
 */
class ElitistEvolution(percent: Double, val config: EvolutionConfig) extends Evolution {
  override def nextGeneration(generation: Generation): Generation = {
    val results = Evolution.extractFitness(generation).toSeq
    val genomes = Evolution.extractGenomes(generation)

    val sortedResults = results.sortWith(_._2 > _._2) // Sorted by fitness. Big to small
    val top = sortedResults.slice(0,(config.poolSize*percent).round.toInt).map(_._1).toIndexedSeq // look at the top percent


    val nextGenomes = ( for(id <- 0 until config.poolSize) yield {
      val i = top(Random.nextInt(top.size)) // Randomly select an id out of the top percent
      id -> genomes(i).mutate // Get the genome to that id and mutate it.
    } ).toMap

    Evolution.groupGenomes(nextGenomes, config)
  }
}
