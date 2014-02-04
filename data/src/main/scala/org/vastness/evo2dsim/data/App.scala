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

package org.vastness.evo2dsim.data

import scalax.file.Path
import scala.concurrent.{Await, Future, future}
import org.vastness.evo2dsim.core.environment.Environment
import org.vastness.evo2dsim.core.data.{Row, Record, DirectRecorder, RecordLevel}
import scala.concurrent.ExecutionContext.Implicits.global
import org.vastness.evo2dsim.core.utils.{InputHandler}
import org.vastness.evo2dsim.core.evolution.{EvolutionRunner, EvolutionConfig}
import scala.concurrent.duration.Duration
import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.core.environment.mixins.settings.BlueTestSettings
import org.vastness.evo2dsim.core.simulator.AgentID

object App {
  def main(args : Array[String]): Unit = {
    val parser = new scopt.OptionParser[DataConfig]("Evo2DSim-Data") {
      cmd("blueTest")  action { (_,c) =>
        c.copy(test = "blueTest") } text "Test affinity to blue light" children(
          opt[Int]('f', "from") action { (x, c) =>
            c.copy(from = Some(x)) } text "Generation to start from",
          opt[Int]('t', "to")  action { (x, c) =>
            c.copy(to = Some(x)) } text "Last Generation",
          arg[String]("<file>...") required() unbounded() action { (x, c) =>
            c.copy(files = c.files :+ x) } text "Directories to work on"
      )
      cmd("redTest") action { (_, c) =>
        c.copy(test = "redTest") } text "Test information processing of red light" children(
        opt[Int]('g', "generation") required() action { (x, c) =>
          c.copy(from = Some(x)) } text "Generation",
        opt[Int]('r', "group") required()  action { (x, c) =>
          c.copy(to = Some(x)) } text "Group",
        arg[String]("<file>...") required() maxOccurs 1  action { (x, c) =>
          c.copy(files = c.files :+ x) } text "Directory to work on"
      )
    }
    // parser.parse returns Option[C]
    parser.parse(args, DataConfig()) map { config =>
     val f =  config.test match {
        case "blueTest" => blueTest(config)
        case "redTest" => redTest(config)
     }
     Await.ready(f, Duration.Inf)
     sys.exit()
    } getOrElse {
      sys.exit(1)
    }
  }

  def redTest(c: DataConfig) : Future[Unit]  = ???

  def blueTest(c: DataConfig) = {
    val f = Future sequence ( c.files map {
      s => future {
        val dir = Path.fromString(s).toAbsolute
        if(dir.exists && dir.isDirectory) runBlue(c.from ,c.to, dir)
        else println(s"Could not find $dir")
      }
    } ).toSeq
    f
  }

  def runRed(gen: Int, group: Int, dir: Path): Unit = {
    val in = new InputHandler(dir)
    val config = in.readEvolutionConfig match {
      case None => throw new Exception("Didn't find a config.")
      case Some(c) => c
    }

    val redTestConfig = config.copy(
      generations = gen,
      evaluationSteps = 100,
      evaluationsPerGeneration = 10,
      groupSize = 1,
      envConf = "0:RedTest",
      evolutionAlgorithm = "", // Stuff breaks if you want to run evolution on this setting.
      rLevel = RecordLevel.Everything.id
    )

    def redCallback(env: Environment) = {}
    runConfig(dir / "redTest", redTestConfig, gen, in, redCallback)
  }

  def runBlue(from: Option[Int], to: Option[Int], dir: Path): Unit = {
    val in = new InputHandler(dir)
    val config = in.readEvolutionConfig match {
      case None => throw new Exception("Didn't find a config.")
      case Some(c) => c
    }
    
    val endGen = to match {
      case None => config.generations
      case Some(end) => if(end <= config.generations) end else config.generations
    }

    val startGen = from match {
      case None => 0
      case Some(start) => if(start < endGen) start else 0
    }

    val blueTestConfig = config.copy(
      generations = endGen,
      evaluationSteps = 10,
      groupSize = 1,
      envConf = "0:BlueTest",
      evolutionAlgorithm = "", // Stuff breaks if you want to run evolution on this setting.
      rLevel = RecordLevel.Nothing.id
    )

    def blueCallback(env: Environment): (AgentID, Float) = env match {
      case e: BlueTestSettings =>
            val d = delta(e.origin) _
            if(e.agents.size != 1) throw new Exception("Only one Agent is supported!")
            val a = e.agents.head
            val distStart = d(e.agent_pos(a._1))
            val distEnd = d(a._2.position)
            val difference = distStart - distEnd // Increase in distance negative, Reduced distance positive
            val result = Math.signum(difference)
            (a._1, result)
      }

    val blue = runConfig(dir / "blueTest", blueTestConfig, startGen, in, blueCallback)
    val results = Await.result(blue, Duration.Inf)

    case class BlueRow(generation: Int, group: Int, result: Float) extends Row
    val gResults = ( results groupBy(_._1.generation) map {
      case (generation, t1) => t1 groupBy(_._1.group) map {
        case (group, t2) =>
          val sum = t2.map(_._2).sum
          Record(BlueRow(generation, group, sum / t2.length))
      }
    } ).flatten.toSeq

    val blueOutput = new DirectRecorder(dir, "blueTest", Seq("Generation", "Group", "BlueTest"))
    blueOutput.write(gResults)
  }

  private def delta(origin: Vec2)(pos: Vec2) : Float = {
    (pos sub origin).length()
  }

  /**
   *
   * @param dir the output dir
   * @param config the evolution config
   * @param startGeneration from startGeneration to config.generations
   * @param in a input hablder to load generations
   * @param callback a callback function that transforms one generation to data
   * @tparam A The output data type
   * @return
   */
  def runConfig[A](dir: Path,
                   config: EvolutionConfig,
                   startGeneration: Int = 0,
                   in: InputHandler,
                   callback: (Environment) => A): Future[Seq[A]] = {
    if(dir.nonExistent) dir.createDirectory()
    val fs = for{generation <- startGeneration until config.generations} yield {
      val f = ( in.readGeneration(generation) map {
        genomes =>
          println(s"Loaded generation $generation")
          EvolutionRunner.groupEvaluations[A](genomes, dir,generation)(config)(callback, wait = true)
      } ).getOrElse(future { Seq.empty })
      Await.ready(f, Duration.Inf) // Block so we don't run out of memory.
    }
    val f = Future sequence fs
    f map (_.flatten)
  }

  def runConfigOnGroup[A](dir: Path,
                   config: EvolutionConfig,
                   startGeneration: Int = 0,
                   in: InputHandler,
                   callback: (Environment) => A): Future[Seq[A]] = {
    if(dir.nonExistent) dir.createDirectory()
    val fs = for{generation <- startGeneration until config.generations} yield {
      val f = ( in.readGeneration(generation) map {
        genomes =>
          println(s"Loaded generation $generation")
          EvolutionRunner.groupEvaluations[A](genomes, dir,generation)(config)(callback, wait = true)
      } ).getOrElse(future { Seq.empty })
      Await.ready(f, Duration.Inf) // Block so we don't run out of memory.
    }
    val f = Future sequence fs
    f map (_.flatten)
  }
}

case class DataConfig(test: String = "", from: Option[Int] = None, to: Option[Int] = None, files: Seq[String] = Seq.empty)