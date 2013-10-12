package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{BodyType, BodyDef}
import org.jbox2d.dynamics
import org.jbox2d.collision.shapes._
import org.vastness.evo2dsim.gui.{WorldBoundarySprite, CircleSprite, BoxSprite}


class World {
  val velocityIteration = 6
  val positionIteration = 3 // recommend iteration values
  val o = new Vec2(0,0)
  def origin = o
  val b2world = new dynamics.World(origin)

  var entityList = List[Entity]()
  var agentList = List[Agent]()
  var agentCounter = 0

  def getEntities = entityList

  //Adds a static box object. Remember width and height are half-units
  def addStaticWorldObject(pos: Vec2, shape: Shape) = {
    val bodyDef = new BodyDef
    bodyDef.position.set(pos)
    bodyDef.`type` = BodyType.STATIC

    val body = b2world.createBody(bodyDef)
    body.createFixture(shape, 1.0f)
  }

  def addStaticBox(pos: Vec2, width: Float, height: Float) = {
    val shape = new PolygonShape
    shape.setAsBox(width,height)
    addEntityToManger(new StaticEntity(new BoxSprite(() => pos, width, height )))
    addStaticWorldObject(pos, shape)
  }

  def addStaticCircle(pos: Vec2, radius: Float) = {
    val shape = new CircleShape
    shape.setRadius(radius)
    addEntityToManger(new StaticEntity(new CircleSprite(() => pos, radius )))
    addStaticWorldObject(pos, shape)
  }

  def createWorldBoundary(edges: Array[Vec2]) = {
    val shape = new ChainShape
    shape.createLoop(edges, edges.length)
    addEntityToManger(new StaticEntity(new  WorldBoundarySprite(() => origin, edges )))
    addStaticWorldObject(new Vec2(0,0), shape)
  }

  def addEntityToManger(e: Entity) = entityList = e :: entityList // constant time prepend
  def addAgent(pos: Vec2) ={
    val a = new Agent(agentCounter, pos, this.b2world)
    agentCounter += 1
    addEntityToManger(a)
    agentList = a :: agentList
  }

  def step(timeStep: Float) = { //TODO Optimize for concurrency
    for(a: Agent <- agentList){
      a.step()
    }
    b2world.step(timeStep, velocityIteration, positionIteration)
  }


}


