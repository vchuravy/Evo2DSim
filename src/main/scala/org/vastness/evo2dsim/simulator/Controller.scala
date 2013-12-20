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

import org.vastness.evo2dsim.neuro._
import org.vastness.evo2dsim.evolution.genomes._
import org.vastness.evo2dsim.evolution.genomes.byte.{ByteGenome, ByteEvolutionManager}
import org.vastness.evo2dsim.evolution.genomes.neat.{NEATGenome, NEATEvolutionManager}

abstract class Controller {
  var nn: Option[NeuronalNetwork] = None
  var genome: Option[Genome] = None

  def sensorNeurons: Set[SensorNeuron]
  def motorNeurons: Set[MotorNeuron]

  def init(g: Genome){
    genome = Some(g)
    nn = Some(NeuronalNetwork(sensorNeurons, motorNeurons, g))
  }

  def getBasicRandomGenome(genomeName: String, ev: EvolutionManager): Genome = {
    val neurons = sensorNeurons ++ motorNeurons
    (genomeName, ev) match {
      case ("ByteGenome", em: ByteEvolutionManager) => ByteGenome.basicRandomGenome(neurons, em)
      case ("NEATGenome", em: NEATEvolutionManager) => NEATGenome.basicRandomGenome(neurons, em)
    }
  }



  def attachToAgent(agent: Agent): Unit

  def activateArtificialSmellMemory(): Unit

  def sensorStep(): Unit

  def controllerStep(){
    nn map (_.step())
  }

  def motorStep(): Unit
}
