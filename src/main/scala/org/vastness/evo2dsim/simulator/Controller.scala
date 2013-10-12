package org.vastness.evo2dsim.simulator

import org.vastness.evo2dsim.neuro.{TransferFunction, MotorNeuron, NeuronalNetwork}

class Controller(agent: Agent) {
  val nn = new NeuronalNetwork()
  val motor = new Motor(agent)

  val leftMotorNeuron = new MotorNeuron(-0.05,TransferFunction.thanh, motor.setLeftMotorSpeed)
  val rightMotorNeuron = new MotorNeuron(-0.05,TransferFunction.thanh, motor.setLeftMotorSpeed)

  nn.addNeuron(leftMotorNeuron)
  nn.addNeuron(rightMotorNeuron)

  def step(){
    nn.step()
    motor.step()
  }

}
