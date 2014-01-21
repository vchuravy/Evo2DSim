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
import org.vastness.evo2dsim.core.evolution.genomes.Genome
import org.vastness.evo2dsim.core.environment.{Environment, EnvironmentBuilder}
import org.vastness.evo2dsim.core.gui.EnvironmentManager
import org.vastness.evo2dsim.core.data.{Recorder, RecordLevel}
import scala.concurrent.ExecutionContext.Implicits.global
import org.vastness.evo2dsim.core.utils.{OutputHandler, InputHandler}
import org.vastness.evo2dsim.core.evolution.{EvolutionRunner, EvolutionConfig}
import org.vastness.evo2dsim.core.evolution.Evolution.Generation
import scala.concurrent.duration.Duration
import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.core.environment.mixins.settings.BlueTestSettings
import org.vastness.evo2dsim.core.simulator.AgentID

object App {
  def main(args : Array[String]) {
    val f = Future sequence ( args map {
      s => future {
        val dir = Path(s)
        run(dir)
      }
    } ).toSeq

    f onSuccess {
      case result => sys.exit(0)
    }

    f onFailure {
      case result =>
        print(result)
        sys.exit(1)
    }
  }

  def run(dir: Path): Unit = {
    var in = new InputHandler(dir)
    val config = in.readEvolutionConfig match {
      case None => throw new Exception("Didn't find a config.")
      case Some(c) => c
    }
    if(RecordLevel.Agents.record(config.recordingLevel)) println("Output already created. Nothing to do here.")
    else {
      val recordingConfig = EvolutionConfig(
        config.timeStep,
        generations = 0,
        config.evaluationSteps,
        config.evaluationsPerGeneration,
        config.poolSize,
        config.groupSize,
        config.envConf,
        evolutionAlgorithm = "", // Stuff breaks if you want to run evolution on this setting.
        config.genomeName,
        config.genomeSettings,
        config.propability,
        RecordLevel.Agents.id
      )
      def callback(envs: Seq[Environment]) = {}
      runConfig(dir, recordingConfig, 0, in, callback)
    }

    in = new InputHandler(dir) // reset InputHandler

    val blueTestConfig = EvolutionConfig(
      config.timeStep,
      generations = 0,
      evaluationSteps = 10,
      config.evaluationsPerGeneration,
      config.poolSize,
      groupSize = 1,
      envConf = "0:BlueTest",
      evolutionAlgorithm = "", // Stuff breaks if you want to run evolution on this setting.
      config.genomeName,
      config.genomeSettings,
      config.propability,
      RecordLevel.Nothing.id
    )

    def callback(envs: Seq[Environment]):Seq[(Int, Int, Float)] = {
      val results = for (env <- envs) yield {
        env match {
          case e: BlueTestSettings =>
            val d = delta(e.origin) _
            if(e.agents.size != 1) throw new Exception("Only one Agent is supported!")
            val a = e.agents.head
            (a._1, norm(d(e.agent_pos(a._1)) - d(a._2.position)))

        }
      }
      val gResults = results groupBy(_._1.generation) map {
        case (generation, t1) => t1 groupBy(_._1.group) map {
          case (group, t2) =>
            (generation, group, t2.map(_._2).sum / t2.length)
        }
      }
      gResults.flatten.toSeq
    }

    val blue = runConfig(dir / "blueTest", blueTestConfig, 0, in, callback)
    val results = Await.result(blue, Duration.Inf).flatten

    val blueOutput = new Recorder(dir, "blueTest", Seq("Generation", "Group", "BlueTest"))
    blueOutput.write(results map(d => Seq(d._1, d._2, d._3)))
  }

  private def delta(origin: Vec2)(pos: Vec2) : Float = {
    (pos sub origin).length()
  }

  private def norm(f: Float): Float = {
    if(f == 0f) 0 else if(f < 0f) -1 else 1
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
                   callback: (Seq[Environment]) => A ): Future[Seq[A]] = {
    val fs = for{generation <- startGeneration until config.generations} yield {
      in.readGeneration(generation) map {
        genomes =>
          val gF = EvolutionRunner.groupEvaluations(genomes, dir)(config.environment(generation))(config)
          gF map callback
      }
    }
    Future sequence fs.flatten
  }
}
