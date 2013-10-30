package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.apache.commons.math3.util.FastMath

class Motor(agent: Agent) {
  val UPPER_OUTPUT_LIMIT = 0.15 // m/s  taken from Cooperative Hole Avoidance in a Swarm-bot
  val LOWER_OUTPUT_LIMIT = -0.15

  val UPPER_INPUT_LIMIT = 1.0
  val LOWER_INPUT_LIMIT = -1.0

  val a = (UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT)/(UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)
  val b = UPPER_OUTPUT_LIMIT - UPPER_INPUT_LIMIT*(UPPER_OUTPUT_LIMIT-LOWER_OUTPUT_LIMIT)/(UPPER_INPUT_LIMIT-LOWER_INPUT_LIMIT)

  def transform(x: Double) = a*x+b //Linear transformation

  private var leftMotorVelocity = 0.0
  private var rightMotorVelocity = 0.0

  def setLeftMotorVelocity(x: Double) {
    leftMotorVelocity = transform(x)
  }

  def setRightMotorVelocity(x: Double) {
    rightMotorVelocity = transform(x)
  }

  //taken from enki speed control
  def forwardVelocity = (rightMotorVelocity + leftMotorVelocity) / 2
  def velocity = new Vec2((forwardVelocity * FastMath.cos(agent.body.getAngle)).toFloat, (forwardVelocity * FastMath.sin(agent.body.getAngle)).toFloat)
  def angularVelocity = (rightMotorVelocity-leftMotorVelocity)/(4*agent.radius) // 2* wheel distants


  //TODO: Check if velocities or forces are better..
  def step() {
    agent.setLinearVelocity(velocity)
    agent.setAngularVelocity(angularVelocity.toFloat)
  }
}
