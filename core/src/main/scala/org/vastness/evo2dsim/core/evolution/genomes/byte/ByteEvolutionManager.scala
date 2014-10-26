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

import org.vastness.evo2dsim.core.evolution.genomes.{Genome, EvolutionManager}
import org.vastness.evo2dsim.core.neuro.{Neuron, TransferFunction}

class ByteEvolutionManager( val probability: Double = 0.01,
                            val standardTransferFunction: TransferFunction = TransferFunction.THANH)
                            extends EvolutionManager {

  var blueprint: Set[Neuron] = Set.empty
  def getBasicRandomGenome: Genome = ByteGenome.basicRandomGenome(blueprint, this)
}
