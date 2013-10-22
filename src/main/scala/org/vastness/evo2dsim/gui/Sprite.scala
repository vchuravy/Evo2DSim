package org.vastness.evo2dsim.gui

import org.jbox2d.common.Vec2
import java.awt.{BasicStroke, Graphics2D}
import scala.annotation.tailrec

/**
 * Implements all possible Sprites
 * Note: AWT is used to upper-left hand corner for coordinates
 * @param p returns the center position of the sprite
 */
abstract class Sprite(p: () => Vec2) {
    val conversionFactor = 200 // From Meters to Pixel 0.1m in the physical World are 20 pixel
    def position = conversionToPixel(p())
    def draw(g2: Graphics2D)
    def conversionToPixel(f: Float) = conversionFactor*f
    def conversionToPixel(v: Vec2) = v.mul(conversionFactor)
}

class BoxSprite(p: () => Vec2, width: Float, height: Float) extends Sprite(p) {
  val w = conversionToPixel(width).toInt
  val h = conversionToPixel(height).toInt
  def draw(g2: Graphics2D) = g2.fillRect((position.x - w/2).toInt, (position.y - h/2).toInt , w, h)
}

class CircleSprite(p: () => Vec2, radius: Float)  extends Sprite(p) {
  val d = 2 * conversionToPixel(radius).toInt
  def draw(g2: Graphics2D) = g2.fillOval(position.x.toInt - d/2, position.y.toInt - d/2, d, d)
}

class WorldBoundarySprite(p: () => Vec2, edges: Array[Vec2]) extends Sprite(p) {
  def vectorsToPoints(v: Array[Vec2])  =  _vectorsToPoints(v.reverse, List[Int](), List[Int]())

  @tailrec
  private def _vectorsToPoints(v: Array[Vec2], x: List[Int], y: List[Int]) : (List[Int],List[Int]) = {
    if (v.isEmpty) (x,y)
    else _vectorsToPoints(v.tail, conversionToPixel(v.head.x).toInt :: x, conversionToPixel(v.head.y).toInt :: y )
  }
  val e = vectorsToPoints(edges)

  def draw(g2: Graphics2D){
    g2.setStroke(new BasicStroke(3))
    g2.drawPolygon(e._1.toArray, e._2.toArray, edges.length)
  }
}
