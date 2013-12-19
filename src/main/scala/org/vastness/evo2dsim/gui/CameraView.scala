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

package org.vastness.evo2dsim.gui

import org.vastness.evo2dsim.teem.enki.sbot.SBotLightSensor
import java.awt
import scala.swing.{Dimension, Graphics2D, Component}

class CameraView(lightSensor: SBotLightSensor) extends Component {
  val s = 4
  val y = 2 * s
  val x = s / (lightSensor.resolution / lightSensor.fov)

  override def paintComponent(g: Graphics2D) {
    super.paintComponent(g)
    g.setBackground(awt.Color.WHITE)
    var j = 0
    for((color, array) <- lightSensor.getVisionStrip) {
      val c  = color.underlying
      var i = 0
      for(v <- array) {
        val (red, blue, green ) = (c.getRed * v, c.getBlue * v, c.getGreen * v)
        g.setColor(new awt.Color(red.toInt, blue.toInt, green.toInt))
        g.fillRect(i * x, j * y, x, y)
        i += 1
      }
      j += 1
    }
  }

  minimumSize = new Dimension(360 * s, lightSensor.getVisionStrip.size * y)
  preferredSize = minimumSize
}
