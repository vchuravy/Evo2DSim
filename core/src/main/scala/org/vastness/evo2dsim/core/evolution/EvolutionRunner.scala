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
import org.vastness.evo2dsim.core.environment.Environment
import org.vastness.evo2dsim.core.evolution.genomes.EvolutionManager
import org.vastness.evo2dsim.core.agents.sbot.SBotController
import org.vastness.evo2dsim.core.evolution.Evolution.{Genomes, Generation}
import org.vastness.evo2dsim.core.utils.OutputHandler
import org.vastness.evo2dsim.core.data._, Record._
import org.vastness.evo2dsim.core.simulator.AgentID

class EvolutionRunner(c: EvolutionConfig) extends Recordable {
  val now = Calendar.getInstance().getTime
  val dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
  val timeStamp = dateFormat.format(now)

  val evo: Evolution = EvolutionBuilder(c)

  val envString = c.envSetup.sortBy(_._1.start).map(_._2.name).mkString("-")
  val dir = (Path("results") resolve s"${timeStamp}_${c.evolutionAlgorithm}_${envString}_${c.genomeName}_${c.propability}_${c.genomeSettings}").createDirectory()
  val output = new OutputHandler(dir, true)

  val signalStrategy = new DirectRecorder(dir, "SignallingStrategy", Seq("Generation", "Group", "Iteration", "Strategy"))

  output.writeEvolutionConfig(c)
  println(s"Results are saved in: $dir")

  override def dataHeader = Seq("Generation", "Max", "Min", "Mean", "Variance", "GroupMax", "GroupMin", "GroupMean", "GroupVariance", "Diversity")
  private var _dataRow = Record.empty
  override def dataRow = _dataRow

  object Timer extends Recordable{
    private var _dataRow = Record.empty
    def dataRow_=(d: Record){
      _dataRow = d
    }
    override def dataRow = _dataRow
    override def dataHeader = Seq("Generation", "Total", "Sim", "Setup", "NextGen")

    case class TRow(idx: Int, total: Long, sim: Long, setup: Long, nextGen: Long) extends Row
  }

  private def run(startGeneration: Generation): Generation = {
    val outputStats = Recorder(dir, "Stats", this)
    val outputTimer = Recorder(dir, "Times", Timer)

    var idx = 0
    var generation: Generation = startGeneration

    while(idx < c.generations) {
      val generationStartTime = System.nanoTime()

      EnvironmentManager.clean()

      assert(c.evaluationSteps > 0, "In Simulation mode evaluationSteps has to be bigger than zero.")
      val fResult =  EvolutionRunner.groupEvaluations(generation, dir / "output", idx)(c)(callback)
      val environmentSetupTime = System.nanoTime()

      val fFitness = fResult map { r => r.flatMap(_._1) }
      val fSignalStrategy = fResult map { r => r collect {case (_, (gen, g, i, Some(s))) => SigS(gen, g, i, s)}}

      fSignalStrategy onSuccess {
        case rows => signalStrategy.write(rows.map(Record.apply))
      }

      val fR = fFitness map { values => evaluate(values, generation)}
      val result = Await.result(fR, Duration.Inf)
      val simulationFinishedTime = System.nanoTime()

      output.writeGeneration(idx, result)

      idx +=1

      _dataRow = collectStats(idx, result)
      outputStats.step()

      if(idx < c.generations) generation = evo.nextGeneration(idx, result)
      assert(generation.size == c.poolSize)

      val generationFinishedTime = System.nanoTime()
      def timeSpent(t1: Long, t2: Long) = {
        TimeUnit.SECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS)
      }

      val timeTotalSpent = timeSpent(generationStartTime, generationFinishedTime)
      val timeSetupSpent = timeSpent(generationStartTime, environmentSetupTime)
      val timeSimSpent = timeSpent(environmentSetupTime, simulationFinishedTime)
      val timeNextGenSpent = timeSpent(simulationFinishedTime, generationFinishedTime)

      println(s"Generation $idx done")
      Timer.dataRow = Record(Timer.TRow(idx, timeTotalSpent, timeSimSpent, timeSetupSpent, timeNextGenSpent))
      outputTimer.step()
      if(idx < c.generations) println("Starting next generation.")
      else println("We are done here :)")
    }
    generation
  }

  private def callback(env: Environment): (Seq[(AgentID, Double)], (Int, Int, Int, Option[Double])) = {
    val f = ( for((id, a) <- env.agents)
      yield id -> a.fitness ).toSeq

    val s = (env.generation, env.group, env.iteration, env.signallingStrategy)
    (f, s)
  }

  private def evaluate(values: Seq[(AgentID, Double)], generation: Generation): Generation = {
    val fitnessValuesPerAgent  = values.groupBy(e => e._1)
    val evaluation = fitnessValuesPerAgent.map((e) => (e._1, e._2.foldLeft(0.0)(_ + _._2)))

    //Update the fitness values
    for((id, fitness) <- evaluation)
      yield id -> (fitness / c.evaluationsPerGeneration , generation(id)._2)
  }

  def start() {
    val time = System.nanoTime()

    val em = EvolutionManager(c.genomeName, c.propability, c.genomeSettings)
    em.blueprint = new SBotController().blueprint
    val genomes: Genomes = ( for(id <- 0 until c.poolSize) yield {
      val g = em.getBasicRandomGenome
      id -> g
    } ).toMap

    val startGeneration = Evolution.groupGenomes(0, genomes, c)
    run(startGeneration)
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
    sys.exit()
  }

  def collectStats(generation: Int, data: Generation): Record = {
    val results = data.map(_._2._1).toSeq

    val max = results.max
    val min = results.min
    val mean = results.sum / results.size
    val variance = results.foldLeft(0.0) {(acc, x) => acc + math.pow(x - mean,2)} / results.size


    val groups = results.grouped(c.groupSize).toSeq map {l => l.sum / c.groupSize}
    val (gMax, gMin, gMean) =
      if (groups.isEmpty) (0.0, 0.0, 0.0)
      else (groups.max, groups.min, groups.sum / groups.size)

    val gVar = if (groups.isEmpty) 0.0 else groups.foldLeft(0.0) {(acc, x) => acc + math.pow(x - gMean,2)} / groups.size
    Record(SRow(generation, max, min, mean, variance, gMax, gMin, gMean, gVar, diversity(data)))
  }

  private def diversity(generation: Generation): Double = {
    val genomes = generation map (_._2._2)
    val distances =
      for(g1 <- genomes; g2 <- genomes)
        yield g1.distance(g2)
    distances.sum
  }

}

object EvolutionRunner {
  def identityCallback(e: Environment) = e
  def groupEvaluations[A](generation: Generation, dir: Path, idx: Int)
                      (config: EvolutionConfig)
                      (callback: (Environment) => A, wait: Boolean = false): Future[Seq[A]] = {
    val groupsById = generation.groupBy{case (id, _) => id.group}
    val groups =
      if(groupsById.size != config.poolSize / config.groupSize) {
        val g = generation.grouped(config.groupSize).toIndexedSeq
        val newGroups = for(i <- g.indices) yield {
          i -> g(i)
        }
        newGroups.toMap
      } else
        groupsById

    val env = config.environment(idx)

    val fEnvs: Future[Seq[A]] = {
      val gF = ( for ((g, group) <- groups) yield {
        val eF = Future sequence ( for (i <- 0 until config.evaluationsPerGeneration) yield {
          val e = env(config.timeStep, config.evaluationSteps)

          e.generation = idx
          e.group = g
          e.iteration = i

          e.initializeStatic()
          e.initializeAgents(group)
          EnvironmentManager.addEnvironment(e)
          if(config.recordingLevel.record(RecordLevel.Nothing)) {
            e.startRecording(config.recordingLevel, i, dir)
          }
          future {
            e.run()
          }
          e.p.future map callback
        } )
        if(wait) Await.ready(eF, Duration.Inf) else eF// When we have a lot of groups creating them all would consume to much memory.
      } ).toSeq
      val f = Future sequence gF
      f map (_.flatten)
    }
    fEnvs
  }
}

case class SigS(idx: Int, group: Int, iter: Int, strategy: Double) extends Row
case class SRow(idx: Int, max: Double, min: Double, mean: Double, variance: Double, gMax: Double, gMin: Double, gMean: Double, gVar: Double, diversity: Double) extends Row

