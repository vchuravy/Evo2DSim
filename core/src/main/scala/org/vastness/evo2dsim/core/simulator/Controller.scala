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

package org.vastness.evo2dsim.core.simulator

import org.vastness.evo2dsim.core.neuro._
import org.vastness.evo2dsim.core.evolution.genomes._
import org.vastness.evo2dsim.core.data.Recordable
import org.vastness.evo2dsim.core.data.Record.Record
import org.vastness.evo2dsim.core.data.Record

abstract class Controller extends Recordable {
  var nn: Option[NeuronalNetwork] = None
  var genome: Option[Genome] = None

  def sensorNeurons: Set[SensorNeuron]
  def motorNeurons: Set[MotorNeuron]

  def init(g: Genome){
    genome = Some(g)
    nn = Some(NeuronalNetwork(sensorNeurons, motorNeurons, g))
  }

  /**
   * Returns the sensor and motor neurons necessary to create a genome.
   * @return Set of Neurons
   */
  def blueprint: Set[Neuron] = sensorNeurons ++ motorNeurons

  def attachToAgent(agent: Agent): Unit

  def activateArtificialSmellMemory(): Unit

  def sensorStep(): Unit

  def controllerStep(){
    nn map (_.step())
  }

  def reset() = nn map (_.reset())

  def motorStep(): Unit

  def dataHeader: Seq[String] = nn map {_.dataHeader} getOrElse Seq.empty
  def dataRow: Record = nn map {_.dataRow} getOrElse Record.empty
}
