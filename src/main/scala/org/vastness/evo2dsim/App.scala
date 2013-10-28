package org.vastness.evo2dsim

import org.vastness.evo2dsim.simulator.{Entity, Simulator}
import org.vastness.evo2dsim.gui._
import javax.swing.{SwingUtilities, JFrame}
import org.jbox2d.common.Vec2
import java.util.Timer
import org.vastness.evo2dsim.simulator.food.StaticFoodSource
import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}

/**
 * @author Valentin Churavy
 */
object App {

  var timer = new Timer
  var running = true
  val HERTZ = 30
  val gui = new GUI
  var environments = List.empty[Environment]
  def visibleEntities = environments match {
    case x :: xs => x.sim.entities
    case Nil => List.empty[Entity]
  }

  private def render() {
    gui.getWorldView.repaint()
  }

  def loop() {
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


  def main(args : Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[Int]('t', "timeStep") action { (x, c) =>
        c.copy(timeStep = x) } text "Time step in ms"
      opt[Int]('s', "simSpeed") action { (x, c) =>
        c.copy(simSpeed = x) } text "Simulation speed"
    }

    parser.parse(args, Config()) map { config =>
      timer = new Timer()
      environments ::= new BasicEnvironment(config.timeStep, config.simSpeed, 1000 )
      environments.head.initialize()

      SwingUtilities.invokeLater(new Runnable() {
        override def run() {
          val frame: JFrame = new JFrame("GUI")
          frame.setContentPane(gui.getPanel)
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
          frame.pack()
          frame.setVisible(true)
        }
      })
      loop()
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(timeStep: Int = 50, simSpeed: Int = 1)
}
