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

import org.vastness.evo2dsim.neuro.TransferFunction
import org.vastness.evo2dsim.evolution.genomes.EvolutionManager

class NEATEvolutionManager(val probability: Double,
                           val standardTransferFunction: TransferFunction = TransferFunction.THANH) extends EvolutionManager {

  var neuronIDMap: Map[Int, Int] = Map.empty
  var innovationNumberMap: Map[(NEATNode, NEATNode), Int] = Map.empty

  private var currentNeuronID_ = -1
  private def nextNeuronID: Int = {
    currentNeuronID_ += 1
    currentNeuronID_
  }
  def currentNeuronID = currentNeuronID_

  private var innovationNumber_ = -1
  private def nextIN: Int = {
    innovationNumber_ += 1
    innovationNumber_
  }

  def nextInnovationNumber(from: NEATNode, to:NEATNode): Int = innovationNumberMap.get((from, to)) match {
    case Some(id) => id
    case None =>
      val id = nextIN
      innovationNumberMap += (from, to) -> id
      id
  }

  def nextNeuronID(iN: Int): Int = neuronIDMap.get(iN) match {
    case Some(id) => id
    case None =>
      val id = nextNeuronID
      neuronIDMap += iN -> id
      id
  }
}