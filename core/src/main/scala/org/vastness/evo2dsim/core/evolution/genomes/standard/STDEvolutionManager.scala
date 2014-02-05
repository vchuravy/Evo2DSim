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

import org.vastness.evo2dsim.core.neuro._
import org.vastness.evo2dsim.core.evolution.genomes.{NodeTag, Genome, EvolutionManager}

class STDEvolutionManager( val probability: Double,
                           val standardTransferFunction: TransferFunction,
                           val recurrent: Boolean,
                           val numberOfHiddenNeurons: Int)
                           extends EvolutionManager {

  var blueprint: Set[Neuron] = Set.empty

  def getBasicRandomGenome: Genome = {
    // Define Helper Function
    def connect(froms: Set[STDNode], tos: Set[STDNode]): Set[STDConnection] =
      for(from <- froms;
          to   <- tos)
      yield STDConnection(from, to, random)


    var id = -1
    val nodes = for(n <- blueprint) yield {
      id += 1
      STDNode(n.tag, id, n.bias, n.t_func, n.data)
    }

    val inputNodes = nodes.filter(_.tag == NodeTag.Sensor)
    val outputNodes = nodes.filter(_.tag == NodeTag.Motor)
    val hiddenNodes = ( for(i <- 1 to numberOfHiddenNeurons) yield {
      STDNode(NodeTag.Hidden, id + i, random, standardTransferFunction, s"Hidden$i")
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
    STDGenome(newNodes, connections, this)
  }

}

object STDEvolutionManager {
  def apply(propability: Double, t_func: TransferFunction, settings: String) = {
    val (r, n) = parse(settings)
    new STDEvolutionManager(propability, t_func, r, n)
  }

  def parse(settings: String): (Boolean, Int) = settings.split(":").toList match {
    case x :: List(y) => (x.toBoolean, y.toInt)
  }
}

