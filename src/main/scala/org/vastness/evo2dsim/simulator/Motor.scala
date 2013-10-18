package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2

class Motor(agent: Agent) {
  val UPPER_OUTPUT_LIMIT = 5 // N?
  val LOWER_OUTPUT_LIMIT = -5

  val UPPER_INPUT_LIMIT = 1.0
  val LOWER_INPUT_LIMIT = -1.0

  val a = (UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT)/(UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)
  val b = UPPER_OUTPUT_LIMIT - UPPER_INPUT_LIMIT*(UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT)/(UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)

  def transform(x: Double) = a*x+b //Linear transformation

  private var leftMotorForce = 0.0
  private var rightMotorForce = 0.0

  def setLeftMotorForce(x: Double) {
    leftMotorForce = transform(x)
  }

  def setRightMotorForce(x: Double) {
    rightMotorForce = transform(x)
  }

  //taken from enki speed control
  def forwardForce = (rightMotorForce + leftMotorForce) / 2
  def force = new Vec2((forwardForce * math.cos(agent.body.getAngle)).toFloat, (forwardForce * math.sin(agent.body.getAngle)).toFloat)
  def torque = (rightMotorForce-leftMotorForce)/(4*agent.radius) // 2* wheel distants


  //TODO: This is a very quick implementation, that might just work.
  def step() {
    agent.applyForce(force)
    agent.applyTorque(torque.toFloat)
    //agent.applyForceAtLocalPoint(new Vec2(0,leftMotorForce.toFloat), new Vec2(-agent.radius,0))
    //agent.applyForceAtLocalPoint(new Vec2(0,rightMotorForce.toFloat), new Vec2(agent.radius,0))
  }
}
