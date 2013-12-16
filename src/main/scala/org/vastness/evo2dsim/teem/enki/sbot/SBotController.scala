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

import org.vastness.evo2dsim.simulator.{Agent, Motor, Controller}
import org.vastness.evo2dsim.neuro.{SensorNeuron, MotorNeuron, TransferFunction}
import org.vastness.evo2dsim.evolution.Genome
import scala.concurrent._, ExecutionContext.Implicits.global


abstract class SBotController extends Controller {
  val motor = new Motor()
  val leftMotorNeuron = new MotorNeuron(0,TransferFunction.THANH, motor.setLeftMotorVelocity)
  val rightMotorNeuron = new MotorNeuron(0,TransferFunction.THANH, motor.setRightMotorVelocity)
  val lightSwitch = new MotorNeuron(0, TransferFunction.BINARY)

  val foodSensorNeuron = new SensorNeuron(0,TransferFunction.THANH)
  val lightSensor = new SBotLightSensor(4, 0)
  val lightNeurons = lightSensor.getNeurons

  val motorNeurons = List(leftMotorNeuron, rightMotorNeuron, lightSwitch)
  val sensorNeurons = foodSensorNeuron :: lightNeurons

  nn.addNeurons(sensorNeurons ++ motorNeurons)

  protected def size: Int = {
    sensorNeurons.size * motorNeurons.size
  }

  override def attachToAgent(agent: Agent) = {
    motor.attachToAgent(agent)
    foodSensorNeuron.s_func = () => future{agent.currentReward}

    agent match {
      case sBot: SBot => {
        lightSwitch.m_func = (x: Double) => sBot.light.active_ = x == 1
        lightSensor.attachToAgent(sBot)
      }
      case _ => println("Warning you just attached a SBotController to an agent that is not of type SBot")
    }
  }

  override def fromGenome(genome: Genome) {
    val (currentID, neurons, synapses) = genome.toSerializedNN
    nn.initializeNetwork(currentID, neurons, synapses) // Note safe to call because Motors and Sensors are initialized
  }

  override def sensorStep(){
    lightSensor.step()
  }

  override def motorStep(){
    motor.step()
  }
}
