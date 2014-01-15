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

package org.vastness.evo2dsim.core.agents.sbot

import org.vastness.evo2dsim.core.simulator.{Agent, Motor, Controller}
import org.vastness.evo2dsim.core.neuro._
import spire.implicits._


class SBotController extends Controller {
  val motor = new Motor()
  val leftMotorNeuron = new MotorNeuron(-1, 0,TransferFunction.THANH,"LeftMotor")(motor.setLeftMotorVelocity)
  val rightMotorNeuron = new MotorNeuron(-1, 0,TransferFunction.THANH, "RightMotor")(motor.setRightMotorVelocity)
  val lightSwitch = new MotorNeuron(-1, 0, TransferFunction.BINARY, "Light")()

  val foodSensorNeuron = new SensorNeuron(-1, 0,TransferFunction.THANH, data = "Food")()
  val lightSensor = new SBotLightSensor(4, 0)
  val lightNeurons = lightSensor.getNeurons

  val motorNeurons = Set(leftMotorNeuron, rightMotorNeuron, lightSwitch)
  val sensorNeurons = lightNeurons + foodSensorNeuron

  protected def size: Int = {
    sensorNeurons.size * motorNeurons.size
  }

  override def activateArtificialSmellMemory() {
    foodSensorNeuron.memory = true
  }

  override def attachToAgent(agent: Agent) = {
    motor.attachToAgent(agent)
    foodSensorNeuron.s_func = () => agent.currentReward

    agent match {
      case sBot: SBot => {
        lightSwitch.m_func = (x: NumberT) => sBot.light.active_ = x == 1.0
        lightSensor.attachToAgent(sBot)
      }
      case _ => println("Warning you just attached a SBotController to an agent that is not of type SBot")
    }
  }

  override def sensorStep(){
    lightSensor.step()
  }

  override def motorStep(){
    motor.step()
  }
}
