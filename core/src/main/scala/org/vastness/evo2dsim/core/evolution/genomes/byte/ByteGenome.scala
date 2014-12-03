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

package org.vastness.evo2dsim.core.evolution.genomes.byte

import org.vastness.evo2dsim.core.evolution.genomes.{NodeTag, Genome}
import scala.util.Random
import org.vastness.evo2dsim.core.neuro.Neuron

case class ByteGenome(nodes: Set[ByteNode], connections: Set[ByteConnection], em: ByteEvolutionManager) extends Genome with Binary {
  type Self = ByteGenome
  type SelfNode = ByteNode
  type SelfConnection = ByteConnection

  def name: String = "ByteGenome"

  val p = em.probability

  def mutate: ByteGenome = ByteGenome(mutateNodes(p), mutateConnections(p), em)

  private def mutateConnections(p: Double): Set[SelfConnection] = connections map (_.mutate(p))

  private def mutateNodes(p: Double) = em.bias_evolution match {
    case true => nodes map (_.mutate(p))
    case false => nodes
  }

  def crossover(other: ByteGenome): ByteGenome = this //TODO

  def hamming(x: Byte, y: Byte): Int = popcount((x ^ y).toByte)

  def distance(other: Genome): Double = other match {
    case other: Self => {
      val nodesDistances = zipper(nodesMap, other.nodesMap) map {
        case (a, b) => hamming(a.v_bias, b.v_bias)
      }

      val connDistances = zipper(connectionMap, other.connectionMap) map {
        case (a, b) => hamming(a.v_weight, b.v_weight)
      }

      return nodesDistances.sum + connDistances.sum
    }
    case _ => ???
  }
}

object ByteGenome {
  def basicRandomGenome(neurons: Set[_ <:Neuron], em: ByteEvolutionManager): ByteGenome = {
    var id = -1
    val nodes = for(n <- neurons) yield {
      id += 1
      ByteNode(n.tag, id, bMapper.mapToByte(n.bias), n.t_func, n.data)
    }
    val inputNodes = nodes.filter(_.tag == NodeTag.Sensor)
    val outputNodes = nodes.filter(_.tag == NodeTag.Motor)
    var idC = -1
    val connections =
      for(from <- inputNodes;
          to   <- outputNodes) yield {
        idC += 1
        ByteConnection(from.id, to.id, bMapper.mapToByte(Random.nextDouble()))
      }
    ByteGenome(nodes, connections, em)
  }
  object bMapper extends Binary
}