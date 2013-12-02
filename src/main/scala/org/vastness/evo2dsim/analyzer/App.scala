package org.vastness.evo2dsim.analyzer

import org.vastness.evo2dsim.gui._
import java.util.Timer
import spray.json._
import org.vastness.evo2dsim.utils.MyJsonProtocol._
import javax.swing.{SwingUtilities, JFrame}
import scalax.file.Path
import scalax.io.{Input, Resource}
import org.vastness.evo2dsim.evolution
import org.vastness.evo2dsim.evolution.Genome

class App {
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
      opt[String]('g', "generations") action { (x, c) =>
        c.copy(path = x) } text "Evaluation directory"
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


    loop() // starting render loop
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(timeStep: Int = 50, path: String = "")

  def loadGeneration(dir: Path, generation: Int): Map[Int, (Double, Genome)] = {
    val genFile = dir resolve "Gen_%04d.json".format(generation)
    val input: Input = Resource.fromInputStream(genFile.inputStream())
    val gen = input.string.asJson.convertTo[Map[String, (Double, Genome)]]
    gen.map(x => x._1.toInt -> x._2)
  }
}
