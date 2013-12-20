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

package org.vastness.evo2dsim.evolution.genomes.neat

import org.vastness.evo2dsim.neuro._
import scala.util.Random
import org.vastness.evo2dsim.evolution.genomes.{NodeTag, Genome}

case class NEATGenome(nodes: Set[NEATNode] = Set.empty,
                      connections: Set[NEATConnection] = Set.empty, nm: NEATEvolutionManager ) extends Genome {
  val name = "NEATGenome"

  type A = NumberT
  type Self = NEATGenome
  type SelfNode = NEATNode
  type SelfConnection = NEATConnection

  override def mutate: NEATGenome = {
    val p = nm.probability
    this.mutateNeat(p).addConnection(p).addNode(p)
  }

  private def addConnection(p: Double): NEATGenome = {
    if(Random.nextDouble() <= p) {
      val possibleConnections = Random shuffle findUnconnectedNodes
      if(possibleConnections.size > 0) {
      val c = possibleConnections.head match {
        case (from, to) => NEATConnection(from, to, random, true, nm.nextInnovationNumber(from, to))
      }
      NEATGenome(nodes, connections + c, nm)
      } else this
    } else this
  }

  private def addNode(p: Double): NEATGenome = {
    if(Random.nextDouble() <= p) {
      var newNodes = nodes
      var newConnections = connections
      Random.shuffle(connections.filter(_.enabled)).headOption map { c => // Only place neurons on top of enabled connections
        newConnections -= c
        newConnections += c.disable
        val node = NEATNode(NodeTag.Hidden, nm.nextNeuronID(c.innovationNumber), random, nm.standardTransferFunction, "")
        newConnections += NEATConnection(c.from, node, one, true, nm.nextInnovationNumber(c.from, node))
        newConnections += NEATConnection(node, c.to, c.weight, true, nm.nextInnovationNumber(node, c.to))
        newNodes += node
      }
      NEATGenome(newNodes, newConnections, nm)
    }
    else this
  }

  private def findUnconnectedNodes = (
    for(from <- nodes) yield {
      val connectedNodes = connections filter (_.from == from) map (_.to)
      val unconnectedNodes = nodes filterNot {n => connectedNodes contains n}
      for (to <- unconnectedNodes) yield from -> to
    }
  ).flatten

  private def mutateNeat(p: Double): NEATGenome = NEATGenome(mutateNodes(p), mutateConnections(p), nm)

  private def mutateConnections(p: Double): Set[SelfConnection] =
    connections map { c =>
      if(Random.nextDouble <= p) c.mutate else c
    }

  private def mutateNodes(p: Double) =
    nodes map { n =>
      if(Random.nextDouble() <= p) n.mutate else n
    }

  override def crossover(other: NEATGenome): NEATGenome = {
    val temp_nodes = (nodes ++ other.nodes).toList //we might have nodes with the same id but different biases
    val nIds = temp_nodes groupBy (_.id)
    val new_nodes = ( for((_, ns) <- nIds) yield {
      val l = ns.length
      if (l == 1) ns.head
      else if ( l < 1) Random.shuffle(ns).head
      else throw new Exception
    } ).toSet

    val temp_conn = (connections ++ other.connections).toList
    val innovNs = temp_conn groupBy (_.innovationNumber)
    val new_connections = ( for ((_, cs) <- innovNs) yield {
      val l = cs.length
      if (l == 1) cs.head
      else if ( l < 1) Random.shuffle(cs).head
      else throw new Exception
    }).toSet
    new NEATGenome(new_nodes, new_connections, nm)
  }
}
object NEATGenome {
  def basicRandomGenome(neurons: Set[_ <:Neuron], em: NEATEvolutionManager): NEATGenome = {
    var id = -1
    val nodes = for(n <- neurons) yield {
      id += 1
      NEATNode(n.tag, id, n.bias, n.t_func, n.data)
    }
    val inputNodes = nodes.filter(_.tag == NodeTag.Sensor)
    val outputNodes = nodes.filter(_.tag == NodeTag.Motor)
    var idC = -1
    val connections =
      for(from <- inputNodes;
          to   <- outputNodes) yield {
        idC += 1
        NEATConnection(from, to, Random.nextDouble(), true, idC)
      }
    NEATGenome(nodes, connections, em)
  }
}
