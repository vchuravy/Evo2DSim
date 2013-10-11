package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{Body, BodyType, BodyDef}
import org.jbox2d.collision.shapes.CircleShape

class Agent(id: Int, pos: Vec2, world: org.jbox2d.dynamics.World) {
  //Defines BodyDef
  val bodyDef = new BodyDef
  bodyDef.position.set(pos)
  bodyDef.`type` = BodyType.DYNAMIC
  bodyDef.userData = this

  val shape = new CircleShape
  shape.setRadius(0.2f) //Agents have a 20cm big

  val body = world.createBody(bodyDef)
  body.createFixture(shape,1.0f) // Density is 1

  def applyTorque(torque: Float) = body.applyTorque(torque)
  def applyForce(force: Vec2) = body.applyForceToCenter(force)
}
