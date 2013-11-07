package org.vastness.evo2dsim

import org.vastness.evo2dsim.gui._
import javax.swing.{SwingUtilities, JFrame}
import java.util.Timer
import org.vastness.evo2dsim.evolution.SUSEvolution

/**
 * @author Valentin Churavy
 */
object App {
  var timer = new Timer
  var running = true
  val HERTZ = 30
  val gui = new GUI

  private def render() {
    gui.getWorldView.repaint()
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


  def main(args : Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[Int]('t', "timeStep") action { (x, c) =>
        c.copy(timeStep = x) } text "Time step in ms"
    }

    parser.parse(args, Config()) map { config =>
      SwingUtilities.invokeLater(new Runnable() {
        override def run() {
          val frame: JFrame = new JFrame("GUI")
          frame.setContentPane(gui.getPanel)
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
          frame.pack()
          frame.setVisible(true)
        }
      })

      val evo = new SUSEvolution(config.numberOfIndiviums, config.groupSize, config.stepsPerEvaluation, config.generation, config.evaluationPerGeneration, config.timeStep)
      loop() // starting render loop
      evo.start()
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(timeStep: Int = 50, generation: Int = 4, stepsPerEvaluation: Int = 3000, evaluationPerGeneration:Int = 3,  numberOfIndiviums:Int = 2000, groupSize: Int = 10)
}
