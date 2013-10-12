package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{BodyType, BodyDef}
import org.jbox2d.collision.shapes.CircleShape
import org.vastness.evo2dsim.gui.CircleSprite

class Agent(id: Int, pos: Vec2, world: org.jbox2d.dynamics.World) extends Entity{
  val radius = 0.2f
  //Defines BodyDef
  val bodyDef = new BodyDef
  bodyDef.position.set(pos)
  bodyDef.`type` = BodyType.DYNAMIC
  bodyDef.userData = this

  val shape = new CircleShape
  shape.setRadius(radius) //Agents are a 20cm big

  val body = world.createBody(bodyDef)
  body.createFixture(shape,1.0f) // Density is 1

  def sprite = new CircleSprite(body.getPosition, radius)

  def step() = Nil
  def applyTorque(torque: Float) = body.applyTorque(torque)
  def applyForce(force: Vec2) = body.applyForceToCenter(force)
}
