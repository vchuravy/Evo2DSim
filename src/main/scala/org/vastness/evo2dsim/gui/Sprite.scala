package org.vastness.evo2dsim.gui

import org.jbox2d.common.Vec2


abstract class Sprite(p: () => Vec2) {
    val conversionFactor = 200 // From Meters to Pixel 0.1m in the physical World are 20 pixel
    def position = conversionToPixel(p())
    def draw() //TODO
    def conversionToPixel(f: Float) = conversionFactor*f
    def conversionToPixel(v: Vec2) = v.mul(conversionFactor)
}

class BoxSprite(p: () => Vec2, width: Float, height: Float) extends Sprite(p) {
  val w = conversionToPixel(width)
  val h = conversionToPixel(height)
  def draw() = Nil
}

class CircleSprite(p: () => Vec2, radius: Float)  extends Sprite(p) {
  val r = conversionToPixel(radius)
  def draw() = Nil
}

class WorldBoundarySprite(p: () => Vec2, edges: Array[Vec2]) extends Sprite(p) {
  val e = edges.map(conversionToPixel)
  def draw() = Nil
}
