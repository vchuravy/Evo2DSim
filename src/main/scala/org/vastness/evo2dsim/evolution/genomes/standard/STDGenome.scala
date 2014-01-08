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

import org.vastness.evo2dsim.evolution.genomes.{NodeTag, Genome}
import org.vastness.evo2dsim.neuro._

case class STDGenome(nodes: Set[STDNode] = Set.empty,
                     connections: Set[STDConnection] = Set.empty,
                     em: STDEvolutionManager) extends Genome {

  val name = "STDGenome"

  type Self = STDGenome
  type SelfNode = STDNode
  type SelfConnection = STDConnection

  def crossover(other: STDGenome) = ???
  def mutate = ???
}

object STDGenome {
  def basicRandomGenome(neurons: Set[_ <:Neuron], em: STDEvolutionManager, numberOfHiddenNodes: Int, recurrent: Boolean): STDGenome = {
    var id = -1
    val nodes = for(n <- neurons) yield {
      id += 1
      STDNode(n.tag, id, n.bias, n.t_func, n.data)
    }
    val inputNodes = nodes.filter(_.tag == NodeTag.Sensor)
    val outputNodes = nodes.filter(_.tag == NodeTag.Motor)
    val hiddenNodes = ( for(i <- 1 until numberOfHiddenNodes) yield {
      STDNode(NodeTag.Hidden, id + i, random, em.standardTransferFunction, s"Hidden${id + i}")
    } ).toSet
    val directConnections =
        connect(inputNodes, hiddenNodes) ++ connect(hiddenNodes, outputNodes)
    val recurrentConnections =
      if(recurrent) {
        connect(outputNodes, hiddenNodes) ++ connect(hiddenNodes, inputNodes) ++ connect(hiddenNodes, hiddenNodes)
      }
      else Set.empty[STDConnection]

    val connections = directConnections ++ recurrentConnections
    val newNodes = nodes ++ hiddenNodes
    STDGenome(newNodes, connections, em)
  }
  private def connect(froms: Set[STDNode], tos: Set[STDNode]): Set[STDConnection] =
    for(from <- froms;
        to   <- tos)
    yield STDConnection(from, to, random)
}
