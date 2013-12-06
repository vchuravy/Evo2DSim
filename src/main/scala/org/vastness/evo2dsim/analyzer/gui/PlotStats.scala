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

import scalax.file.Path
import breeze.plot._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class PlotStats(val base: Path){
  val f = Figure()
  var stats: ImagePanel = new ImagePanel

  val gplFile = base resolve "Evo2DSim.gpl"
  loadGPLFile()

  private def loadGPLFile() {
    val resource = getClass.getClassLoader.getResource("Evo2DSim.gpl")
    Path(resource.toURI) match {
      case Some(path) => path.copyTo(gplFile, replaceExisting = true)
      case None => println("Could not resolve resource")
    }
  }

  def render(obj: Any) {
    obj match {
      case s: String => {
        base resolve s match {
          case p: Path if p.exists && (p / "Stats.csv").exists => loadStats(p)
          case _ => println("Could not resolve")
        }
      }
      case _ => println("No String")
    }
  }

  def loadStats(dir: Path) {
    val statsFile = dir resolve "Stats.csv"
    if (statsFile.nonExistent) return
    future {
      val png = dir resolve "Stats.png"
      val cmd = Seq("gnuplot",
                    "-e", "in='%s';out='%s'".format(statsFile.path, png.path),
                    gplFile.path)
      import scala.sys.process._
      Process(cmd).!
      if (png.exists) png else throw new Exception("PNG creation failed.")
    } onComplete {
      case Success(path) =>
        stats.imagePath = path.path
        stats.repaint()
      case Failure(t) =>
        println("Could not create file, Error: " + t)
        stats.clear()
        stats.repaint()
    }
  }
}
