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

package org.vastness.evo2dsim.evolution.genomes.standard

import org.vastness.evo2dsim.evolution.genomes.Genome
import scala.util.Random

case class STDGenome(nodes: Set[STDNode] = Set.empty,
                     connections: Set[STDConnection] = Set.empty,
                     em: STDEvolutionManager) extends Genome {

  val name = "STDGenome"

  type Self = STDGenome
  type SelfNode = STDNode
  type SelfConnection = STDConnection

  def crossover(other: STDGenome) = this // TODO: Add a proper crossover
  def mutate = STDGenome(mutateNodes(em.probability), mutateConnections(em.probability), em)

  private def mutateConnections(p: Double): Set[SelfConnection] =
    connections map { c =>
      if(Random.nextDouble <= p) c.mutate else c
    }

  private def mutateNodes(p: Double) =
    nodes map { n =>
      if(Random.nextDouble() <= p) n.mutate else n
    }
}
