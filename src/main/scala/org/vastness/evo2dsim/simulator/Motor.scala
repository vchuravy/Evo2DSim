package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2

class Motor(agent: Agent) {
  val UPPER_OUTPUT_LIMIT = 5.0 // m/s?
  val LOWER_OUTPUT_LIMIT = -5.0

  val UPPER_INPUT_LIMIT = 1.0
  val LOWER_INPUT_LIMIT = -1.0

  val a = (UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT)/(UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)
  val b = UPPER_OUTPUT_LIMIT - UPPER_INPUT_LIMIT*(UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT)/(UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)

  def transform(x: Double) = a*x+b //Linear transformation

  private var leftMotorSpeed = 0.0
  private var rightMotorSpeed = 0.0

  def setLeftMotorSpeed(x: Double) {
    leftMotorSpeed = transform(x)
  }

  def setRightMotorSpeed(x: Double) {
    rightMotorSpeed = transform(x)
  }

  //TODO: This is a very quick implementation, that might just work.
  def step() {
    agent.applyForceAtLocalPoint(new Vec2(0,leftMotorSpeed.toFloat), new Vec2(-agent.radius,0))
    agent.applyForceAtLocalPoint(new Vec2(0,rightMotorSpeed.toFloat), new Vec2(agent.radius,0))
  }
}
