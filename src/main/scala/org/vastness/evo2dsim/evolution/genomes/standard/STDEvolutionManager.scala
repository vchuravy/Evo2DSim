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

import org.vastness.evo2dsim.neuro.TransferFunction
import org.vastness.evo2dsim.evolution.genomes.{Genome, EvolutionManager}

class STDEvolutionManager(val probability: Double,
                          val standardTransferFunction: TransferFunction = TransferFunction.THANH)
                          extends EvolutionManager {
  def init(g: Genome) {}
}

