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

package org.vastness.evo2dsim.evolution

import scala.concurrent._, duration._, ExecutionContext.Implicits.global

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.Calendar

import scalax.file._

import org.vastness.evo2dsim.gui.EnvironmentManager
import org.vastness.evo2dsim.environment.{EnvironmentBuilder, Environment}
import org.vastness.evo2dsim.evolution.genomes.{EvolutionManager, Genome}
import org.vastness.evo2dsim.teem.enki.sbot.SBotController
import org.vastness.evo2dsim.evolution.Evolution.Generation
import org.vastness.evo2dsim.utils.OutputHandler
import org.vastness.evo2dsim.data.{RecordLevel, Recorder, Recordable}

class EvolutionRunner(name: String,
                      poolSize: Int,
                      groupSize: Int,
                      evaluationSteps: Int,
                      generations:Int,
                      evaluationPerGeneration: Int,
                      timeStep: Int,
                      envSetup: Seq[(Range,EnvironmentBuilder)],
                      genomeName: String,
                      genomeSettings: String,
                      propability: Double,
                      recordLevel: RecordLevel) extends Recordable {

  val now = Calendar.getInstance().getTime
  val dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
  val timeStamp = dateFormat.format(now)

  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  val evo: Evolution = EvolutionBuilder(name)(poolSize)

  val envString = envSetup.sortBy(_._1.start).map(_._2.name).mkString("-")
  val dir = (Path("results") resolve s"${timeStamp}_${name}_${envString}_${genomeName}_${propability}_$genomeSettings").createDirectory()
  val output = new OutputHandler(dir, true)

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

  private def run(startGenomes: Map[Int, (Double, Genome)]): Map[Int, (Double, Genome)] = {
    val outputStats = new Recorder(dir, "Stats", this)
    val outputTimer = new Recorder(dir, "Times", Timer)

    var generation = 0
    var genomes: Map[Int, (Double, Genome)] = startGenomes

    while(generation < generations) {
      val generationStartTime = System.nanoTime()

      EnvironmentManager.clean()

      val envBuilders = envSetup filter {case (range, _) => range contains generation} map {_._2}
      val envBuilder = envBuilders.size match {
        case 0 => throw new Exception(s"Could not find an environment for generation $generation.")
        case 1 => envBuilders.head
        case 2 =>
          println(s"Warning: Two possible environment for generation $generation")
          println("Selecting the last.")
          envBuilders.tail.head
        case _ =>
          println(s"Warning: Multiple possible environment for generation $generation")
          println("Selecting randomly.")
          scala.util.Random.shuffle(envBuilders).head
      }

      assert(evaluationSteps > 0, "In Simulation mode evaluationSteps has to be bigger than zero.")
      val futureEvaluations =  groupEvaluations(genomes.toList, generation)(envBuilder)
      assert(futureEvaluations.size == evaluationPerGeneration*(poolSize / groupSize))

      val evaluationFuture = Future sequence futureEvaluations
      val environmentSetupTime = System.nanoTime()

      val evaluatedEnvironments = Await.result(evaluationFuture, Duration.Inf)
      val simulationFinishedTime = System.nanoTime()

      val extractedFitnessValues =
        evaluatedEnvironments.map( env => (for((id, a) <- env.agents) yield id -> a.fitness).toSeq)
      val fitnessValuesPerAgent  = extractedFitnessValues.flatten.groupBy(e => e._1)
      val evaluation = fitnessValuesPerAgent.map((e) => (e._1, e._2.foldLeft(0.0)(_ + _._2)))
      val evaluationFinishedTime = System.nanoTime()

      val results: Generation = for((id, fitness) <- evaluation) yield id -> (fitness / evaluationPerGeneration , genomes(id)._2)
      output.writeGeneration(generation, results)

      generation +=1

      _dataRow = collectStats(results.map(_._2._1).toSeq)
      outputStats.step()

      if(generation < generations) genomes = evo.nextGeneration(results)
      assert(genomes.size == poolSize)

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
      if(generation < generations) println("Starting next generation.")
      else println("We are done here :)")
    }
    genomes
  }


  def groupEvaluations(genomes: List[(Int, (Double, Genome))], generation: Int)(env: EnvironmentBuilder): Seq[Future[Environment]] = {
    val gs = genomes.sortBy(_._1).grouped(groupSize).toIndexedSeq
    ( for (g <- gs.indices) yield {
      for (i <- 0 until evaluationPerGeneration) yield {
        val e = env(timeStep, evaluationSteps)
        e.initializeStatic()
        e.initializeAgents(gs(g).toMap)
        EnvironmentManager.addEnvironment(e)
        if(recordLevel.record(RecordLevel.Nothing)) {
          e.startRecording(recordLevel, generation, g, i, dir)
        }
        future {
          e.run()
        }
        e.p.future
      }
    } ).flatten.seq
  }

  def start() {
    val time = System.nanoTime()

    val em = EvolutionManager(genomeName, propability, genomeSettings)
    em.blueprint = new SBotController().blueprint
    val genomes = for(id <- (0 until poolSize).par) yield {
      val g = em.getBasicRandomGenome
     (id, (0.0, g))
    }

    run(Map(genomes.seq: _*))
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


    val groups = results.grouped(groupSize).toSeq map {l => l.sum / groupSize}
    val (gMax, gMin, gMean) =
      if (groups.isEmpty) (0.0, 0.0, 0.0)
      else (groups.max, groups.min, groups.sum / groups.size)

    val gVar = if (groups.isEmpty) 0.0 else groups.foldLeft(0.0) {(acc, x) => acc + math.pow(x - gMean,2)} / groups.size
    Seq(max, min, mean, variance, gMax, gMin, gMean, gVar)
  }
}
