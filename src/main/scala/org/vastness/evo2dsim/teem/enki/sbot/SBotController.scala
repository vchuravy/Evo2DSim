package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.simulator.{Agent, Motor, Controller}
import org.vastness.evo2dsim.neuro.{SensorNeuron, MotorNeuron, TransferFunction}
import org.vastness.evo2dsim.evolution.Genome


abstract class SBotController extends Controller {
  val motor = new Motor()
  val leftMotorNeuron = new MotorNeuron(0,TransferFunction.thanh, motor.setLeftMotorVelocity)
  val rightMotorNeuron = new MotorNeuron(0,TransferFunction.thanh, motor.setRightMotorVelocity)
  val lightSwitch = new MotorNeuron(0, TransferFunction.binary)

  val foodSensorNeuron = new SensorNeuron(0,TransferFunction.thanh)
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
    foodSensorNeuron.s_func = () => agent.currentReward

    agent match {
      case sBot: SBot => {
        lightSwitch.m_func = (x: Double) => sBot.light.active = x == 1
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
