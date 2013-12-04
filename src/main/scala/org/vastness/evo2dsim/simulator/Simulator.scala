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
  var agentCounter = 0

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
    addEntityToManger(new StaticEntity(new BoxSprite(pos, Color.BLACK, width, height )))
    addStaticWorldObject(pos, shape)
  }

  def addStaticCircle(pos: Vec2, radius: Float){
    val shape = new CircleShape
    shape.setRadius(radius)
    addEntityToManger(new StaticEntity(new CircleSprite(pos, Color.BLACK, radius )))
    addStaticWorldObject(pos, shape)
  }

  def createWorldBoundary(edges: Array[Vec2]) {
    val shape = new ChainShape
    shape.createLoop(edges, edges.length)
    addEntityToManger(new StaticEntity(new  WorldBoundarySprite(origin, Color.BLACK, edges )))
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
  def addAgent(pos: Vec2, agentType: Agents.Value) : Agent = agentType match {
      case Agents.SBot => addSBot(pos)
      case Agents.SBotControllerLinear => addSBotWithLinearController(pos)
      case Agents.SBotControllerLinearRandom => {
        val a = addSBotWithLinearController(pos)
        a.controller.get.initializeRandom(random.nextDouble)
        a
      }
      case Agents.SBotControllerLinearZero => {
        val a = addSBotWithLinearController(pos)
        a.controller.get.initializeZeros()
        a
      }
    }

  private def addSBot(pos: Vec2): Agent = {
    val a = new SBot(agentCounter, pos, this)
    addAgent(a)
  }

  private def addSBotWithLinearController(pos: Vec2): Agent = {
    val a = addSBot(pos)
    a.controller = Option(new SBotControllerLinear)
    a.controller.get.attachToAgent(a)
    a
  }

  private def addAgent(agent: Agent) : Agent = {
    agentCounter += 1
    addEntityToManger(agent)
    agentList = agent :: agentList
    agent
  }

  def addFoodSource(pos: Vec2, radius: Float, activationRange: Float, foodSource: FoodSource){
    val bodyDef = new BodyDef
    bodyDef.position.set(pos)
    bodyDef.`type` = BodyType.STATIC

    val shape = new CircleShape
    shape.setRadius(radius)

    val sensorShape = new CircleShape
    sensorShape.setRadius(activationRange)

    val body = world.createBody(bodyDef)
    val bodyFixtureDef = new FixtureDef
    bodyFixtureDef.density = 1.0f
    bodyFixtureDef.shape = shape

    val e1 = new StaticEntity(new CircleSprite(pos, foodSource.color, radius ))
    addEntityToManger(e1)

    foodSource.initialize(e1, this)

    val e2 = new StaticEntity(new EmptyCircleSprite(pos, foodSource.color, activationRange ))
    addEntityToManger(e2)

    val sensorFixtureDef = new FixtureDef
    sensorFixtureDef.shape = sensorShape
    sensorFixtureDef.isSensor = true
    sensorFixtureDef.density = 0.0f
    sensorFixtureDef.userData = foodSource

    body.createFixture(bodyFixtureDef)
    body.createFixture(sensorFixtureDef)


    foodSourceList += foodSource
  }

  def step(timeStep: Float) {
    agentList.par.foreach(_.sensorStep())
    agentList.par.foreach(_.controllerStep())
    agentList.par.foreach(_.motorStep())

    world.step(timeStep, velocityIteration, positionIteration)

    foodSourceList.par.foreach(_.step())
  }

  object Agents extends Enumeration {
    type Agents = Value
    val SBot, SBotControllerLinear, SBotControllerLinearRandom, SBotControllerLinearZero = Value
  }
}


