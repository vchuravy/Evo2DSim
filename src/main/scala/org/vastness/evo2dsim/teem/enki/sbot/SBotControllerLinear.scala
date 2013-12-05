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

package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.evolution.{BinaryGenome, Genome}

class SBotControllerLinear() extends SBotController() {

  override def toGenome:Genome = BinaryGenome.initialize(nn, mutateBiases = false)

  override def initialize(weights: Array[Double]) {
    nn.generateLinearNetwork(sensorNeurons, motorNeurons, weights)
  }

  override def initializeRandom(random: () => Double){
    val weights = Array.fill(size){random() *2-1}
    initialize(weights)
  }

  override def initializeZeros(){
    val weights = Array.fill(size){0.0}
    initialize(weights)
  }
}
