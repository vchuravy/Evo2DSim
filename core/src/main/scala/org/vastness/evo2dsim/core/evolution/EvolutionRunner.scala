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

package org.vastness.evo2dsim.core.evolution

import scala.concurrent._, duration._, ExecutionContext.Implicits.global

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.Calendar

import scalax.file._

import org.vastness.evo2dsim.core.gui.EnvironmentManager
import org.vastness.evo2dsim.core.environment.{EnvironmentBuilder, Environment}
import org.vastness.evo2dsim.core.evolution.genomes.{EvolutionManager, Genome}
import org.vastness.evo2dsim.core.agents.sbot.SBotController
import org.vastness.evo2dsim.core.evolution.Evolution.{Genomes, Generation}
import org.vastness.evo2dsim.core.utils.OutputHandler
import org.vastness.evo2dsim.core.data.{RecordLevel, Recorder, Recordable}
import org.vastness.evo2dsim.core.simulator.AgentID

class EvolutionRunner(c: EvolutionConfig) extends Recordable {
  val now = Calendar.getInstance().getTime
  val dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
  val timeStamp = dateFormat.format(now)

  val evo: Evolution = EvolutionBuilder(c)

  val envString = c.envSetup.sortBy(_._1.start).map(_._2.name).mkString("-")
  val dir = (Path("results") resolve s"${timeStamp}_${c.evolutionAlgorithm}_${envString}_${c.genomeName}_${c.propability}_${c.genomeSettings}").createDirectory()
  val output = new OutputHandler(dir, true)

  output.writeEvolutionConfig(c)
  println(s"Results are saved in: $dir")

  override def dataHeader = Seq("Generation", "Max", "Min", "Mean", "Variance", "GroupMax", "GroupMin", "GroupMean", "GroupVariance")
  private var _dataRow = Seq.empty[Any]
  override def dataRow = _dataRow

  object Timer extends Recordable{
    private var _dataRow = Seq.empty[Any]
    def dataRow_=(d: Seq[Any]){
      _dataRow = d
    }
    override def dataRow = _dataRow
    override def dataHeader = Seq("Generation", "Total", "Eval", "Sim", "Setup", "NextGen")
  }

  private def run(startGenomes: Generation): Generation = {
    val outputStats = Recorder(dir, "Stats", this)
    val outputTimer = Recorder(dir, "Times", Timer)

    var generation = 0
    var genomes: Generation = startGenomes

    while(generation < c.generations) {
      val generationStartTime = System.nanoTime()

      EnvironmentManager.clean()

      val envBuilder = c.environment(generation)
      assert(c.evaluationSteps > 0, "In Simulation mode evaluationSteps has to be bigger than zero.")
      val futureEvaluations =  EvolutionRunner.groupEvaluations(genomes, dir)(envBuilder)(c)

      val environmentSetupTime = System.nanoTime()

      val evaluatedEnvironments = Await.result(futureEvaluations, Duration.Inf)
      val simulationFinishedTime = System.nanoTime()

      val extractedFitnessValues =
        evaluatedEnvironments.map( env => (for((id, a) <- env.agents) yield id -> a.fitness).toSeq)
      val fitnessValuesPerAgent  = extractedFitnessValues.flatten.groupBy(e => e._1)
      val evaluation = fitnessValuesPerAgent.map((e) => (e._1, e._2.foldLeft(0.0)(_ + _._2)))
      val evaluationFinishedTime = System.nanoTime()

      val results: Generation = for((id, fitness) <- evaluation) yield id -> (fitness / c.evaluationsPerGeneration , genomes(id)._2)
      output.writeGeneration(generation, results)

      generation +=1

      _dataRow = collectStats(results.map(_._2._1).toSeq)
      outputStats.step()

      if(generation < c.generations) genomes = evo.nextGeneration(results)
      assert(genomes.size == c.poolSize)

      val generationFinishedTime = System.nanoTime()
      def timeSpent(t1: Long, t2: Long) = {
        TimeUnit.SECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS)
      }

      val timeTotalSpent = timeSpent(generationStartTime, generationFinishedTime)
      val timeSetupSpent = timeSpent(generationStartTime, environmentSetupTime)
      val timeSimSpent = timeSpent(environmentSetupTime, simulationFinishedTime)
      val timeEvalSpent = timeSpent(simulationFinishedTime, evaluationFinishedTime)
      val timeNextGenSpent = timeSpent(evaluationFinishedTime, generationFinishedTime)

      println(s"Generation $generation done")
      Timer.dataRow = Seq(generation, timeTotalSpent, timeEvalSpent, timeSimSpent, timeSetupSpent, timeNextGenSpent)
      outputTimer.step()
      if(generation < c.generations) println("Starting next generation.")
      else println("We are done here :)")
    }
    genomes
  }

  def start() {
    val time = System.nanoTime()

    val em = EvolutionManager(c.genomeName, c.propability, c.genomeSettings)
    em.blueprint = new SBotController().blueprint
    val genomes: Genomes = ( for(id <- 0 until c.poolSize) yield {
      val g = em.getBasicRandomGenome
      id -> g
    } ).toMap

    val startGeneration = Evolution.groupGenomes(genomes, c)
    run(startGeneration)
    output.finish()
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
    sys.exit()
  }

  def collectStats(results: Seq[Double]): Seq[Any] = {
    val max = results.max
    val min = results.min
    val mean = results.sum / results.size
    val variance = results.foldLeft(0.0) {(acc, x) => acc + math.pow(x - mean,2)} / results.size


    val groups = results.grouped(c.groupSize).toSeq map {l => l.sum / c.groupSize}
    val (gMax, gMin, gMean) =
      if (groups.isEmpty) (0.0, 0.0, 0.0)
      else (groups.max, groups.min, groups.sum / groups.size)

    val gVar = if (groups.isEmpty) 0.0 else groups.foldLeft(0.0) {(acc, x) => acc + math.pow(x - gMean,2)} / groups.size
    Seq(max, min, mean, variance, gMax, gMin, gMean, gVar)
  }
}

object EvolutionRunner {
  def groupEvaluations(genomes: Generation, dir: Path)
                      (env: EnvironmentBuilder)
                      (config: EvolutionConfig): Future[Seq[Environment]] = {
    val groups = genomes.groupBy {case (id, _) => id.group}
    val fEnvs: Seq[Future[Environment]] = (
      for ((g, group) <- groups) yield {
        for (i <- 0 until config.evaluationsPerGeneration) yield {
          val e = env(config.timeStep, config.evaluationSteps)
          e.initializeStatic()
          e.initializeAgents(group)
          EnvironmentManager.addEnvironment(e)
          if(config.recordingLevel.record(RecordLevel.Nothing)) {
            e.startRecording(config.recordingLevel, i, dir)
          }
          future {
            e.run()
          }
          e.p.future
        }
      }
    ).toSeq.flatten
    Future sequence fEnvs
  }
}
