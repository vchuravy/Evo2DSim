package org.vastness.evo2dsim.teem.enki.sbot

import org.vastness.evo2dsim.simulator.{Motor, Controller}
import org.vastness.evo2dsim.neuro.{SensorNeuron, MotorNeuron, TransferFunction}


abstract class SBotController(sbot: SBot) extends Controller(sbot) {
  val motor = new Motor(sbot)

  val leftMotorNeuron = new MotorNeuron(0.05,TransferFunction.thanh, motor.setLeftMotorForce)
  val rightMotorNeuron = new MotorNeuron(-0.05,TransferFunction.thanh, motor.setLeftMotorForce)
  val lightSwitch = new MotorNeuron(0, TransferFunction.thanh,(_) => {} ) // TODO: Implement LightSource and LightSensor

  val foodSensorNeuron = new SensorNeuron(0,TransferFunction.thanh, () => 0 ) //TODO: Implement Food and FoodSensor

  val lightSensor = new SBotLightSensor(sbot, 4)
  val lightNeurons = lightSensor.getNeurons

  val motorNeurons = List(leftMotorNeuron, rightMotorNeuron, lightSwitch)
  val sensorNeurons = foodSensorNeuron :: lightNeurons

  nn.addNeurons(sensorNeurons ++ motorNeurons)

  protected def size: Int = {
    sensorNeurons.size * motorNeurons.size
  }

  override def step(){
    super.step() // Executes nn
    motor.step()
  }
}
