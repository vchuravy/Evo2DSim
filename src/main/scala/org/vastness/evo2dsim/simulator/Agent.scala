package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{BodyType, BodyDef}
import org.jbox2d.collision.shapes.CircleShape
import org.vastness.evo2dsim.gui.CircleSprite

class Agent(id: Int, pos: Vec2, sim: Simulator, val radius: Float, mass: Float) extends Entity{
  //Defines BodyDef
  val bodyDef = new BodyDef
  bodyDef.position.set(pos)
  bodyDef.`type` = BodyType.DYNAMIC
  bodyDef.userData = this
  bodyDef.angularDamping = 0.01f
  bodyDef.linearDamping = 0.01f

  val shape = new CircleShape
  shape.setRadius(radius)

  val body = sim.world.createBody(bodyDef)
  val density = (mass / (Math.PI * radius * radius)).toFloat // Density is influenced by the volume and the mass
  body.createFixture(shape, density)

  var controller:Option[Controller] = None

  def sprite = new CircleSprite(body.getPosition, radius)

  def step() {
    controller match{
      case None => {}
      case Some(c) => c.step()
    }
  }
  def applyTorque(torque: Float) = body.applyTorque(torque)
  def applyForce(force: Vec2) = body.applyForceToCenter(force)
  def applyForceAtLocalPoint(force: Vec2, point: Vec2) = body.applyForce(force, body.getWorldPoint(point))
}
