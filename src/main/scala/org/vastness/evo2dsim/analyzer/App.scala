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

package org.vastness.evo2dsim.analyzer

import java.util.Timer
import java.io.File
import org.vastness.evo2dsim.analyzer.gui.MainWindow
import scala.swing.SwingApplication

object App extends SwingApplication {
  val top = new MainWindow()


  var timer = new Timer
  var running = true
  val HERTZ = 30

  private def render() {
    top.worldView.repaint()
  }

  def loop() {
    timer = new Timer()
    timer.schedule(new RenderLoop, 0, 1000 / HERTZ)//new timer at 30 fps, the timing mechanism
  }

  private class RenderLoop extends java.util.TimerTask
  {
    override def run()
    {
      render()

      if (!running)
      {
        timer.cancel()
      }
    }
  }

  def startup(args : Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[Int]('t', "timeStep") action { (x, c) =>
        c.copy(timeStep = x) } text "Time step in ms"
      opt[File]('p', "path") action { (x, c) =>
        c.copy(path = x) } text "Evaluation directory"
    }

    parser.parse(args, Config()) map { config =>
      top.dataDir = Some(config.path)
      top.timeStep = config.timeStep

      loop() // starting render loop

      top.pack()
      top.visible = true
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(timeStep: Int = 50, path: File = new File("."))
}
