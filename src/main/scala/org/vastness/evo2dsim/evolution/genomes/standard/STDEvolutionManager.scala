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

import org.vastness.evo2dsim.neuro.{Neuron, TransferFunction}
import org.vastness.evo2dsim.evolution.genomes.{Genome, EvolutionManager}

class STDEvolutionManager( val probability: Double,
                           val standardTransferFunction: TransferFunction)
                         ( val recurrent: Boolean,
                           val numberOfHiddenNeurons: Int)
                           extends EvolutionManager {

  var blueprint: Set[Neuron] = Set.empty

  def getBasicRandomGenome: Genome = STDGenome.basicRandomGenome(blueprint, this, numberOfHiddenNeurons, recurrent)
}

object STDEvolutionManager {
  def apply(propability: Double, t_func: TransferFunction, settings: String) = {
    val (r, n) = parse(settings)
    new STDEvolutionManager(propability, t_func)(r, n)
  }
  def parse(settings: String): (Boolean, Int) = settings.split(":").toList match {
    case x :: List(y) => (x.toBoolean, y.toInt)
  }
}

