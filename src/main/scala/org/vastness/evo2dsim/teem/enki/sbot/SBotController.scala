package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.simulator.{Motor, Controller}
import org.vastness.evo2dsim.neuro.{SensorNeuron, MotorNeuron, TransferFunction}
import org.vastness.evo2dsim.evolution.Genome


abstract class SBotController(sbot: SBot) extends Controller(sbot) {
  val motor = new Motor(sbot)

  val leftMotorNeuron = new MotorNeuron(0,TransferFunction.thanh, motor.setLeftMotorVelocity)
  val rightMotorNeuron = new MotorNeuron(0,TransferFunction.thanh, motor.setRightMotorVelocity)

  val lightSwitch = new MotorNeuron(0, TransferFunction.binary, (x: Double) => sbot.light.active = x == 1 )

  val foodSensorNeuron = new SensorNeuron(0,TransferFunction.thanh, () => sbot.currentReward)

  val lightSensor = new SBotLightSensor(sbot, 4, 0)
  val lightNeurons = lightSensor.getNeurons

  val motorNeurons = List(leftMotorNeuron, rightMotorNeuron, lightSwitch)
  val sensorNeurons = foodSensorNeuron :: lightNeurons

  nn.addNeurons(sensorNeurons ++ motorNeurons)

  protected def size: Int = {
    sensorNeurons.size * motorNeurons.size
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
