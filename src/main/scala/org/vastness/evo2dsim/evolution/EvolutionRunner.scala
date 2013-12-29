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
import scala.util.Random
import scala.annotation.tailrec

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.Calendar

import org.apache.commons.compress.archivers.sevenz._

import scalax.file._
import scalaz.{TreeLoc, Tree}
import spray.json._

import org.vastness.evo2dsim.gui.EnvironmentManager
import org.vastness.evo2dsim.environment.{EnvironmentBuilder, Environment}
import org.vastness.evo2dsim.utils.MyJsonProtocol._
import org.vastness.evo2dsim.evolution.genomes.{EvolutionManager, Genome}
import org.vastness.evo2dsim.teem.enki.sbot.SBotController
import java.nio.charset.Charset

class EvolutionRunner(name: String, poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, evaluationPerGeneration: Int, timeStep: Int, envSetup: Seq[(Range,EnvironmentBuilder)], genomeName: String) {
  val now = Calendar.getInstance().getTime
  val dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
  val timeStamp = dateFormat.format(now)

  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  val evo: Evolution = EvolutionBuilder(name)(poolSize)

  val envString = envSetup.sortBy(_._1.start).map(_._2.name).mkString("-")
  val dir = (Path("results") resolve s"${timeStamp}_${name}_${envString}_$genomeName").createDirectory()
  val generationsFile = new SevenZOutputFile((dir / "generations.7z").jfile)
  println(s"Results are saved in: $dir")


  private def run(startGenomes: Map[Int, (Double, Genome)]): Map[Int, (Double, Genome)] = {
    val outputStats =  dir resolve "Stats.csv"
    outputStats.createFile()
    outputStats.append("Generation, Max, Min, Mean, Variance, GroupMax, GroupMin, GroupMean, GroupVariance \n")

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
      val futureEvaluations =  groupEvaluations(genomes.toList)(envBuilder)
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

      val results: Map[Int, (Double, Genome)] = for((id, fitness) <- evaluation) yield id -> (fitness / evaluationPerGeneration , genomes(id)._2)

      val o7 = new SevenZArchiveEntry()
      o7.setName("Gen_%04d.json".format(generation))

      val output = results.map(x => x._1.toString -> x._2).toJson.prettyPrint.getBytes
      o7.setSize(output.size)

      generationsFile.putArchiveEntry(o7)
      generationsFile.write(output)
      generationsFile.closeArchiveEntry()

      generation +=1

      val (max, min, mean, variance, gMax, gMin, gMean, gVar) = collectStats(results.map(_._2._1).toList)
      outputStats.append(s"$generation, $max, $min, $mean, $variance, $gMax, $gMin, $gMean, $gVar \n")

      if(generation < generations) genomes = evo.nextGeneration(results)
      assert(genomes.size == poolSize)

      val generationFinishedTime = System.nanoTime()
      def timeSpent(t1: Long, t2: Long) = {
        TimeUnit.SECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS)
      }
      def logTime(s: String, t: Long) = println(s.format(t/ 60, t % 60))

      val timeTotalSpent = timeSpent(generationStartTime, generationFinishedTime)
      val timeSetupSpent = timeSpent(generationStartTime, environmentSetupTime)
      val timeSimSpent = timeSpent(environmentSetupTime, simulationFinishedTime)
      val timeEvalSpent = timeSpent(simulationFinishedTime, evaluationFinishedTime)
      val timeNextGenSpent = timeSpent(evaluationFinishedTime, generationFinishedTime)

      println(s"Generation $generation done")
      logTime("Generation took %d min %d sec in total",timeTotalSpent)
      logTime("Setup of the simulation took %d min %d sec",timeSetupSpent)
      logTime("Simulation took %d min %d sec", timeSimSpent)
      logTime("Evaluation took %d min %d sec", timeEvalSpent)
      logTime("Preparing the next Generation took %d min %d sec", timeNextGenSpent)
      if(generation < generations) println("Starting next generation.")
      else println("We are done here :)")
    }
    genomes
  }


  def groupEvaluations(genomes: List[(Int, (Double, Genome))])(env: EnvironmentBuilder): Seq[Future[Environment]] = {
    val gs = genomes.sortBy(_._1).grouped(groupSize).toSeq
    ( for (g <- gs.par) yield {
      for (i <- 0 until evaluationPerGeneration) yield {
        val e = env(timeStep, evaluationSteps)
        e.initializeStatic()
        e.initializeAgents(g.toMap)
        EnvironmentManager.addEnvironment(e)
        future {
          e.run()
        }
        e.p.future
      }
    } ).flatten.seq
  }

  def start(em: EvolutionManager) {
    val time = System.nanoTime()
    em.init(new SBotController().getBasicRandomGenome(genomeName, em))
    val genomes = for(id <- (0 until poolSize).par) yield {
     val c = new SBotController()
     val g = c.getBasicRandomGenome(genomeName, em)
     (id, (0.0, g))
    }
    run(Map(genomes.seq: _*))
    generationsFile.finish()
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
    sys.exit()
  }

  def collectStats(results: Seq[Double]): (Double, Double, Double, Double, Double, Double, Double, Double) = {
    val max = results.max
    val min = results.min
    val mean = results.sum / results.size
    val variance = results.foldLeft(0.0) {(acc, x) => acc + math.pow(x - mean,2)} / results.size


    val groups = results.grouped(groupSize) map {l => l.sum / groupSize}

    val gMax = groups.max
    val gMin = groups.min
    val gMean = groups.sum / groups.size
    val gVar = groups.foldLeft(0.0) {(acc, x) => acc + math.pow(x - gMean,2)} / groups.size
    (max, min, mean, variance, gMax, gMin, gMean, gVar)
  }
}
