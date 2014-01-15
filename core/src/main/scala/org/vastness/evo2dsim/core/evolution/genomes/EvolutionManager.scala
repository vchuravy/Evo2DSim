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

package org.vastness.evo2dsim.core.evolution.genomes

import org.vastness.evo2dsim.core.neuro.{Neuron, TransferFunction}
import org.vastness.evo2dsim.core.evolution.genomes.byte.ByteEvolutionManager
import org.vastness.evo2dsim.core.evolution.genomes.neat.NEATEvolutionManager
import org.vastness.evo2dsim.core.evolution.genomes.standard.STDEvolutionManager

trait EvolutionManager {
  def probability: Double
  def standardTransferFunction: TransferFunction
  def blueprint: Set[Neuron] //Getter
  def blueprint_=(b: Set[Neuron]) //Setter
  def getBasicRandomGenome: Genome
}

object EvolutionManager {
  def apply(genomeName: String, propability: Double, genomeSettings: String, t_func: TransferFunction = TransferFunction.THANH): EvolutionManager = genomeName match {
      case "ByteGenome" => new ByteEvolutionManager(propability, t_func)
      case "NEATGenome" => new NEATEvolutionManager(propability, t_func)
      case "STDGenome"  => STDEvolutionManager(propability, t_func, genomeSettings)
    }
}
