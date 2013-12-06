/*
 * This file is part of Evo2DSimAnalyzer.
 *
 * Evo2DSimAnalyzer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSimAnalyzer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSimAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vastness.evo2dsim.analyzer.gui

import swing._

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImagePanel extends Panel
{
  preferredSize = new Dimension(800,600)
  private var _imagePath: Option[String] = None
  private var bufferedImage: Option[BufferedImage] = None

  def imagePath = _imagePath

  def clear() {
    _imagePath = None
    bufferedImage = None
  }

  def imagePath_=(value:String)
  {
    _imagePath = Some(value)
    bufferedImage = _imagePath map { path => ImageIO.read(new File(path))}
  }


  override def paintComponent(g:Graphics2D) {
    super.paintComponent(g)
    bufferedImage map {
      case image => g.drawImage( image, 0, 0, null)
    }
  }
}
