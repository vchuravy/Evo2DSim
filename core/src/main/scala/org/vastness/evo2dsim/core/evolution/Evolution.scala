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

import org.vastness.evo2dsim.core.evolution.genomes.Genome
import org.vastness.evo2dsim.core.evolution.Evolution.Generation
import org.vastness.evo2dsim.core.simulator.AgentID

trait Evolution {
  def config: EvolutionConfig
  def nextGeneration(idx: Int, results: Generation): Generation
}

object Evolution {
  type Generation = Map[AgentID, (Double, Genome)]
  type Genomes = Map[Int, Genome]

  def groupGenomes(genIdx: Int, genomes: Genomes, config: EvolutionConfig): Generation = {
    val groupedGenomes = genomes.grouped(config.groupSize).toIndexedSeq
    val generation: Generation = ( for(group <- groupedGenomes.indices) yield {
      groupedGenomes(group) map {
        case (id, genome) => AgentID(id, group, genIdx) -> (0.0, genome)
      }
    } ).flatten.toMap
    generation
  }

  def extractGenomes(generation: Generation): Genomes = {
    generation map {
      case (id, data) => id.id -> data._2
    }
  }

  def extractFitness(generation: Generation): Map[Int, Double] = {
    generation map {
      case (id, data) => id.id -> data._1
    }
  }
}

