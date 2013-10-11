package org.vastness.evo2dsim.simulator

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.{BodyType, BodyDef}
import org.jbox2d.dynamics
import org.jbox2d.collision.shapes._


class World(timeStep: Float) {
  val velocityIteration = 6
  val positionIteration = 3 // recommend iteration values
  val b2world = new dynamics.World(new Vec2(0,0))

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
    addStaticWorldObject(pos, shape)
  }

  def addStaticCircle(pos: Vec2, radius: Float) = {
    val shape = new CircleShape
    shape.setRadius(radius)
    addStaticWorldObject(pos, shape)
  }

  def createWorldBoundary(edges: Array[Vec2]) = {
    val shape = new ChainShape
    shape.createLoop(edges, edges.length)
    addStaticWorldObject(new Vec2(0,0), shape)
  }

  def step() = b2world.step(timeStep, velocityIteration, positionIteration)

}


