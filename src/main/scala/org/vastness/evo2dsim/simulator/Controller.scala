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

package org.vastness.evo2dsim.simulator

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import org.vastness.evo2dsim.evolution.Genome

abstract class Controller() {
  val nn = new NeuronalNetwork()

  def toGenome: Genome

  def fromGenome(genome: Genome): Unit

  def attachToAgent(agent: Agent): Unit

  def initialize(weights: Array[Double])
  def initializeRandom(random: () => Double)
  def initializeZeros()

  def activateArtificialSmellMemory(): Unit

  def sensorStep(): Unit

  def controllerStep(){
    nn.step()
  }

  def motorStep(): Unit

}
