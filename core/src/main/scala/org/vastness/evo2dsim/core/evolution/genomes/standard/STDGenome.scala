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

package org.vastness.evo2dsim.core.evolution.genomes.standard

import org.vastness.evo2dsim.core.evolution.genomes.Genome
import scala.util.Random

case class STDGenome(nodes: Set[STDNode] = Set.empty,
                     connections: Set[STDConnection] = Set.empty,
                     em: STDEvolutionManager) extends Genome {

  val name = "STDGenome"

  type Self = STDGenome
  type SelfNode = STDNode
  type SelfConnection = STDConnection

  def crossover(other: STDGenome) = this // TODO: Add a proper crossover
  def mutate = {
    val probability = 1.0 / ( nodes.size + connections.size) // 1/l
    STDGenome(mutateNodes(probability), mutateConnections(probability), em)
  }

  private def mutateConnections(p: Double): Set[SelfConnection] =
    connections map {c => if(Random.nextDouble <= p) c.mutate(em.randSource.sample()) else c }

  private def mutateNodes(p: Double) =
    nodes map {n => if(Random.nextDouble <= p) n.mutate(em.randSource.sample()) else n }

  /**
   * Implements euclidean distance
   * @param other
   * @return
   */
  def distance(other: Genome): Double = other match {
    case other: Self => {
      val nodesDistances = zipper(nodesMap, other.nodesMap) map {
      case (a, b) => math.pow(a.bias - b.bias, 2)
      }

      val connDistances = zipper(connectionMap, other.connectionMap) map {
      case (a, b) => math.pow(a.weight - b.weight, 2)
      }

      return math.sqrt(nodesDistances.sum + connDistances.sum)
    }
    case _ => ???
  }
}
