/*
 * This file is part of Evo2DSim.
 *
 * Evo2DSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{BodyType, BodyDef}
import org.jbox2d.collision.shapes.CircleShape
import org.vastness.evo2dsim.gui.{Color, CircleSprite}

abstract class Agent(id: Int, pos: Vec2, angle: Float, val sim: Simulator, val radius: Float, mass: Float) extends Entity{
  //Defines BodyDef
  val bodyDef = new BodyDef
  bodyDef.position.set(pos)
  bodyDef.angle = angle
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

  override def sprite = new CircleSprite(radius)(body.getPosition, color, text)
  override def position = body.getPosition

  var fitness = 0.0
  var currentReward = 0.0

  def text = "%d \n F:%.2f \n CR:%.2f".format(id, fitness, currentReward)

  def sensorStep() {
    controller map { c => c.sensorStep() }
  }

  def controllerStep() {
    controller map { c => c.controllerStep() }
  }
  def motorStep() {
    controller map { c => c.motorStep() }
  }

  def color: Color = Color.BLACK

  def setLinearVelocity(v: Vec2) = body.setLinearVelocity(v)
  def setAngularVelocity(w: Float) = body.setAngularVelocity(w)
  // def applyTorque(torque: Float) = body.applyTorque(torque)
  // def applyForce(force: Vec2) = body.applyForceToCenter(force)
  // def applyForceAtLocalPoint(force: Vec2, point: Vec2) = body.applyForce(force, body.getWorldPoint(point))
}
