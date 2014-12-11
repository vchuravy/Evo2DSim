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

package org.vastness.evo2dsim.analyzer

import java.util.Timer
import java.io.File
import org.vastness.evo2dsim.core.environment.EnvironmentBuilder

import scalax.file.Path
import org.vastness.evo2dsim.analyzer.gui.MainWindow
import scala.swing.SwingApplication

object App extends SwingApplication {
  val top = new MainWindow()


  var timer = new Timer
  var simTimer = new Timer
  var running = true

  var pause = false

  val HERTZ = 30
  var speed = 16

  private def render() {
    top.worldView.repaint()
    top.renderComponents.foreach(_.repaint())
  }

  def renderLoop() {
    timer.cancel()
    timer = new Timer()
    timer.schedule(new RenderLoop, 0, 1000 / HERTZ)//new timer at 30 fps, the timing mechanism
  }

  def simLoop() {
    simTimer.cancel()
    simTimer = new Timer()
    simTimer.schedule(new SimLoop, 0, 1000 / (speed*HERTZ))//new timer at 120 fps, the timing mechanism
  }

  def changeSpeedUp() {
    speed *= 2
    simLoop()
  }

  def changeSpeedDown() {
    val oldSpeed = speed
    speed /= 2
    if(speed*HERTZ <=0) speed = oldSpeed

    simLoop()
  }

  def togglePause() {
    pause = if(!pause) true else false
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

  private class SimLoop extends java.util.TimerTask {
    override def run() {
      if(!pause) top.e map (_.updateSimulation())
      if (!running)
      {
        simTimer.cancel()
      }

      if(!top.e.map(_.running).getOrElse(true) && top.e.map(e => e.stepCounter == e.steps).getOrElse(false)) {
        top.initEnvironment()
        simLoop()
      }
    }
  }

  def startup(args : Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[Int]('t', "timeStep") action { (x, c) =>
        c.copy(timeStep = x) } text "Time step in ms"
      opt[Int]('g', "generation") action { (x, c) =>
        c.copy(generation = x) } text "Generation"
      opt[Int]('n', "group") action { (x, c) =>
        c.copy(group = x) } text "Group"
      opt[File]('p', "path") action { (x, c) =>
        c.copy(path = x) } text "Evaluation directory"
      opt[String]('r', "simRun") action { (x, c) =>
        c.copy(simRun = x) } text "Simulation run directory"
      opt[String]('e', "Environment") action { (x, c) =>
        c.copy(env = x) } text "Environment"
    }

    parser.parse(args, Config()) map { config =>
      top.dataDir = Some(config.path)
      top.evalDir = top.dataDir map (d => Path(d).resolve(config.simRun))
      top.timeStep = config.timeStep
      top.generation = top.evalDir.flatMap(dir => top loadGeneration(dir, config.generation))
      top.group = config.group

      top.envBuilder = EnvironmentBuilder.values.find(_.name == config.env) match {
        case Some(eBuilder) => eBuilder
        case _ => top.envBuilder
      }

      renderLoop() // starting render loop
      simLoop()

      top.pack()
      top.visible = true
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(timeStep: Int = 50, path: File = new File("."), generation: Int = 0, group: Int = 0, simRun:String = ".", env: String = "")
}
