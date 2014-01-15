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

package org.vastness.evo2dsim.core.gui

import org.jbox2d.common.Vec2
import java.awt.{BasicStroke, Graphics2D}
import scala.annotation.tailrec

/**
 * Implements all possible Sprites
 * Note: AWT is used to upper-left hand corner for coordinates
 */
abstract class Sprite(p: => Vec2, color: => Color, text: => String) {

    val conversionFactor = 200 // From Meters to Pixel 0.1m in the physical World are 20 pixel
    def real_position = p
    def position = conversionToPixel(real_position)
    def draw(g2: Graphics2D) {
      g2.setColor(color.underlying)
    }

    def conversionToPixel(f: Float) = conversionFactor*f
    def conversionToPixel(v: Vec2) = v.mul(conversionFactor)

    def drawText(g2: Graphics2D) =  {
      val textColor = color match {
        case Color.BLACK => Color.RED
        case _ => Color.BLACK
      }
      g2.setColor(textColor.underlying)
      var verticalOffset = position.y
      for(line <- text.split("\n")) {
        g2.drawString(line, position.x, verticalOffset)
        verticalOffset += g2.getFontMetrics.getHeight
      }
    }
}

class BoxSprite(width: Float, height: Float)(p: => Vec2, color: => Color, text: => String) extends Sprite(p,color,text) {
  val w = conversionToPixel(width).toInt
  val h = conversionToPixel(height).toInt
  override def draw(g2: Graphics2D){
    super.draw(g2)
    g2.fillRect((position.x - w/2).toInt, (position.y - h/2).toInt , w, h)
  }
}

class CircleSprite(radius: Float)(p: => Vec2, color: => Color, text: => String)  extends Sprite(p,color,text) {
  val d = 2 * conversionToPixel(radius).toInt
  override def draw(g2: Graphics2D){
    super.draw(g2)
    g2.fillOval(position.x.toInt - d/2, position.y.toInt - d/2, d, d)
  }
}

class EmptyCircleSprite(radius: Float)(p: => Vec2, color: => Color, text: => String) extends Sprite(p,color,text) {
  val d = 2 * conversionToPixel(radius).toInt
  override def draw(g2: Graphics2D) {
    super.draw(g2)
    g2.drawOval(position.x.toInt - d/2, position.y.toInt - d/2, d, d)
  }
}

class WorldBoundarySprite(edges: Array[Vec2])(p: => Vec2, color: => Color, text: => String) extends Sprite(p,color,text) {
  def vectorsToPoints(v: Array[Vec2])  =  _vectorsToPoints(v.reverse, List[Int](), List[Int]())

  @tailrec
  private def _vectorsToPoints(v: Array[Vec2], x: List[Int], y: List[Int]) : (List[Int],List[Int]) = {
    if (v.isEmpty) (x,y)
    else _vectorsToPoints(v.tail, conversionToPixel(v.head.x).toInt :: x, conversionToPixel(v.head.y).toInt :: y )
  }
  val e = vectorsToPoints(edges)

  override def draw(g2: Graphics2D){
    super.draw(g2)
    g2.setStroke(new BasicStroke(3))
    g2.drawPolygon(e._1.toArray, e._2.toArray, edges.length)
  }
}
