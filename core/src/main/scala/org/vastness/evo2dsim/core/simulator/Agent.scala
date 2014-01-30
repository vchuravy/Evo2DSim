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

package org.vastness.evo2dsim.core.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{Body, BodyType, BodyDef}
import org.jbox2d.collision.shapes.CircleShape
import org.vastness.evo2dsim.core.gui.{Color, CircleSprite}
import org.vastness.evo2dsim.core.data.Recordable

abstract class Agent(val id: AgentID, pos: Vec2, vAngle: Float, val sim: Simulator, val radius: Float, mass: Float) extends Entity with Recordable{
  //Defines BodyDef
  val bodyDef = new BodyDef
  bodyDef.position.set(pos)
  bodyDef.angle = vAngle
  bodyDef.`type` = BodyType.DYNAMIC
  bodyDef.userData = this
  bodyDef.angularDamping = 0.01f
  bodyDef.linearDamping = 0.01f

  val shape = new CircleShape
  shape.setRadius(radius)

  val body: Body = sim.world.createBody(bodyDef)
  val density = (mass / (Math.PI * radius * radius)).toFloat // Density is influenced by the volume and the mass
  val agentFixture = body.createFixture(shape, density)
  agentFixture.setUserData(this)

  def controller: Controller

  def signalling: Boolean

  override def sprite = new CircleSprite(radius)(body.getPosition, color, text)
  override def position = body.getPosition
  def position_=(p: Vec2) = body.setTransform(p, 0) //Teleports agent.
  def angle = body.getAngle

  var fitness = 0.0
  var currentReward = 0.0

  def text = "%s \n F:%.2f \n CR:%.2f".format(id, fitness, currentReward)

  def sensorStep() {
    controller.sensorStep()
  }

  def controllerStep() {
    controller.controllerStep()
  }
  def motorStep() {
    controller.motorStep()
  }

  def color: Color = Color.BLACK

  def setLinearVelocity(v: Vec2) = body.setLinearVelocity(v)
  def setAngularVelocity(w: Float) = body.setAngularVelocity(w)
  // def applyTorque(torque: Float) = body.applyTorque(torque)
  // def applyForce(force: Vec2) = body.applyForceToCenter(force)
  // def applyForceAtLocalPoint(force: Vec2, point: Vec2) = body.applyForce(force, body.getWorldPoint(point))

  def activateArtificialSmellMemory() {
    controller.activateArtificialSmellMemory()
  }

  def dataHeader: Seq[String] = Seq("posX", "posY", "angle", "fitness", "currentReward")
  def dataRow: Seq[Any] = Seq(position.x, position.y, angle, fitness, currentReward)
}
