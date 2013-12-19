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
import org.jbox2d.dynamics.{FixtureDef, BodyType, BodyDef}
import org.jbox2d.dynamics
import org.jbox2d.collision.shapes._
import org.vastness.evo2dsim.gui._
import org.vastness.evo2dsim.teem.enki.sbot.{SBotControllerLinear, SBot}
import org.vastness.evo2dsim.simulator.light.LightManager
import org.vastness.evo2dsim.simulator.food.FoodSource
import scala.collection.mutable.ArrayBuffer

class Simulator(seed: Long) {
  val random = new scala.util.Random(seed)

  val velocityIteration = 8
  val positionIteration = 4 // recommend iteration values
  val origin = new Vec2(0,0)
  val world = new dynamics.World(origin)
  val lightManager = new LightManager
  world.setContactListener(new ContactListener)

  var entities = List[Entity]()
  var agentList = List[Agent]()

  private val foodSourceList = new ArrayBuffer[FoodSource]()


  //Adds a static box object. Remember width and height are half-units
  def addStaticWorldObject(pos: Vec2, shape: Shape) {
    val bodyDef = new BodyDef
    bodyDef.position.set(pos)
    bodyDef.`type` = BodyType.STATIC

    val body = world.createBody(bodyDef)
    body.createFixture(shape, 1.0f)
  }

  def addStaticBox(pos: Vec2, width: Float, height: Float) {
    val shape = new PolygonShape
    shape.setAsBox(width,height)
    addEntityToManger(new StaticEntity(new BoxSprite(width, height)(pos, Color.BLACK, ""), this))
    addStaticWorldObject(pos, shape)
  }

  def addStaticCircle(pos: Vec2, radius: Float){
    val shape = new CircleShape
    shape.setRadius(radius)
    addEntityToManger(new StaticEntity(new CircleSprite(radius)(pos, Color.BLACK, ""), this))
    addStaticWorldObject(pos, shape)
  }

  def createWorldBoundary(edges: Array[Vec2], text: => String = "") {
    val shape = new ChainShape
    shape.createLoop(edges, edges.length)
    addEntityToManger(new StaticEntity(new  WorldBoundarySprite(edges)(origin, Color.BLACK, text), this))
    addStaticWorldObject(new Vec2(0,0), shape)
  }

  def addEntityToManger(e: Entity) {entities = e :: entities} // constant time prepend

  /**
   * Adds a agent of a certain type to the sim
   * TODO: To much boilerplate, refactor into something more flexible
   * @param pos position of the agent
   * @param agentType  agent type
   * @return the created agent
   */
  def addAgent(pos: Vec2, angle:Float, agentType: Agents.Value, id: Int) : Agent = agentType match {
      case Agents.SBot => addSBot(pos, angle, id)
      case Agents.SBotControllerLinear => addSBotWithLinearController(pos, angle, id)
      case Agents.SBotControllerLinearRandom =>
        val a = addSBotWithLinearController(pos, angle, id)
        a.controller.get.initializeRandom(random.nextDouble)
        a
      case Agents.SBotControllerLinearZero =>
        val a = addSBotWithLinearController(pos, angle, id)
        a.controller.get.initializeZeros()
        a
    }

  private def addSBot(pos: Vec2, angle:Float, id: Int): Agent = {
    val a = new SBot(id, pos, angle, this)
    addAgent(a)
  }

  private def addSBotWithLinearController(pos: Vec2, angle:Float, id: Int): Agent = {
    val a = addSBot(pos, angle, id)
    a.controller = Option(new SBotControllerLinear)
    a.controller.get.attachToAgent(a)
    a
  }

  private def addAgent(agent: Agent) : Agent = {
    addEntityToManger(agent)
    agentList = agent :: agentList
    agent
  }

  def addFoodSource(pos: Vec2, foodSource: FoodSource){
    val bodyDef = new BodyDef
    bodyDef.position.set(pos)
    bodyDef.`type` = BodyType.STATIC

    val shape = new CircleShape
    shape.setRadius(foodSource.radius)

    val feedingSensorShape = new CircleShape
    feedingSensorShape.setRadius(foodSource.activationRange)

    val smellSensorShape = new CircleShape
    smellSensorShape.setRadius(foodSource.smellRange)

    val body = world.createBody(bodyDef)
    val bodyFixtureDef = new FixtureDef
    bodyFixtureDef.density = 1.0f
    bodyFixtureDef.shape = shape

    def reward = foodSource.reward
    def max = foodSource.max
    def current = foodSource.feeders.size

    def text = "R:%.2f \nM:%d \nC:%d".format(reward, max, current)

    val e1 = new StaticEntity(new CircleSprite(foodSource.radius)(pos, foodSource.color, text), this)
    addEntityToManger(e1)

    foodSource.initialize(e1, this)

    val e2 = new StaticEntity(new EmptyCircleSprite(foodSource.activationRange)(pos, foodSource.color, ""), this)
    addEntityToManger(e2)

    val e3 = new StaticEntity(new EmptyCircleSprite(foodSource.smellRange)(pos, Color.CYAN, ""), this)
    addEntityToManger(e3)


    val feedingSensorFixtureDef = new FixtureDef
    feedingSensorFixtureDef.shape = feedingSensorShape
    feedingSensorFixtureDef.isSensor = true
    feedingSensorFixtureDef.density = 0.0f
    feedingSensorFixtureDef.userData = (true, foodSource)

    val smellSensorFixtureDef = new FixtureDef
    smellSensorFixtureDef.shape = smellSensorShape
    smellSensorFixtureDef.isSensor = true
    smellSensorFixtureDef.density = 0.0f
    smellSensorFixtureDef.userData = (false, foodSource)

    body.createFixture(bodyFixtureDef)
    body.createFixture(feedingSensorFixtureDef)
    body.createFixture(smellSensorFixtureDef)

    foodSourceList += foodSource
  }

  def step(timeStep: Float) {
    agentList foreach { a => a.sensorStep() }
    agentList foreach { a => a.controllerStep() }
    agentList foreach { a => a.motorStep() }

    world.step(timeStep, velocityIteration, positionIteration)

    foodSourceList foreach { f => f.step() }
  }

  object Agents extends Enumeration {
    type Agents = Value
    val SBot, SBotControllerLinear, SBotControllerLinearRandom, SBotControllerLinearZero = Value
  }
}


