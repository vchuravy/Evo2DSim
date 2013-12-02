package org.vastness.evo2dsim.analyzer

import org.vastness.evo2dsim.gui._
import java.util.Timer
import spray.json._
import org.vastness.evo2dsim.utils.MyJsonProtocol._
import javax.swing.SwingUtilities
import scala.swing.Frame
import scalax.file.Path
import scalax.io.Input
import java.io.File
import org.vastness.evo2dsim.evolution.Genome
import org.vastness.evo2dsim.environment.BasicEnvironment
import scala.concurrent._
import ExecutionContext.Implicits.global

object App {
  var timer = new Timer
  var running = true
  val HERTZ = 30
  val gui = new GUI

  private def render() {
    gui.worldView.repaint()
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
      opt[File]('p', "path") action { (x, c) =>
        c.copy(path = x) } text "Evaluation directory"
    }

    parser.parse(args, Config()) map { config =>

     SwingUtilities.invokeLater(new Runnable() {
       override def run() {
         val frame: Frame = new Frame() {
           override def contents = Seq(gui.panel)
         }
         frame.pack()
       }
     })

    loop() // starting render loop
    tui(config.path, config.timeStep)
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(timeStep: Int = 50, path: File = new File("."))

  def loadGeneration(dir: Path, generation: Int): Map[Int, (Double, Genome)] = {
    val genFile = dir resolve "Gen_%04d.json".format(generation)
    val input: Input = genFile.inputStream()
    val gen = input.string.asJson.convertTo[Map[String, (Double, Genome)]]
    gen.map (x => x._1.toInt -> x._2)
  }

  def loadStats(dir: Path): List[(Int, (Double, Double, Double, Double))] = {
    val statsFile = dir resolve "Stats.csv"
    val in = statsFile.inputStream()
    val csv: Traversable[Array[String]] = in.lines().map (_ split ',').tail
    csv.map {
      case Array(index, max, min, mean, variance) => index.toInt -> (max.toDouble, min.toDouble, mean.toDouble, variance.toDouble)
    }.toList
  }

  def findBestGroupInGen(gen: Map[Int, (Double, Genome)], size: Int) = {
    val groups = gen.toList.sortBy(_._1).grouped(size)
    val groupPerformance = groups.map(e => e.foldLeft[Double](0.0){ case (acc, (_, (fitness, _))) => acc + fitness}).toIndexedSeq
    ( for(i <- groupPerformance.indices) yield (i, groupPerformance(i) / size) ).toList.sortBy(_._2)
  }

  def tui(path: File, timeStep: Int) {
    val dir = Path(path)
    val stats = loadStats(dir)
    for((index,(max, min, mean, v)) <- stats.sortBy(_._2._3)) {
          printf("Gen %04d, max: %.2f, min: %.2f, mean: %.2f, var: %.2f \n", index, max, min,mean ,v)
    }
    print("Please select a generation: ")
    val genIndex = readInt()
    val gen = loadGeneration(dir, genIndex)

    for((index, gP) <- findBestGroupInGen(gen, 10)) {
      printf("GI: %4d, %.2f \n", index, gP)
    }
    print("Select a group to evaluate: ")
    val group = readInt()

    val e = new BasicEnvironment(timeStep, 0)
    e.initializeStatic()
    e.initializeAgents(gen.grouped(10).toIndexedSeq(group))
    EnvironmentManager.addEnvironment(e)
    future {
      e.run()
    }

  }
}
