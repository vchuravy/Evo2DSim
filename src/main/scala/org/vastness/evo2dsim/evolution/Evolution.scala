package org.vastness.evo2dsim.evolution

import java.io.File
import java.util.concurrent.TimeUnit
import scala.pickling._
import spray.json._
import org.vastness.evo2dsim.utils.MyJsonProtocol._

import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.vastness.evo2dsim.gui.EnvironmentManager
import org.vastness.evo2dsim.environment.BasicEnvironment
import org.vastness.evo2dsim.teem.enki.sbot.SBotControllerLinear
import scala.util.Random
import java.util.Calendar
import java.text.SimpleDateFormat

abstract class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, evaluationPerGeneration: Int, timeStep: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  val now = Calendar.getInstance().getTime
  val dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
  val timeStamp = dateFormat.format(now)

  new File("%s".format(timeStamp)).mkdir()

  def nextGeneration(results: Seq[(Int, (Double, Genome))]): Map[Int, (Double, Genome)]

  private def run(startGenomes: Map[Int, (Double, Genome)]): Map[Int, (Double, Genome)] = {
    var generation = 0
    var genomes: Map[Int, (Double, Genome)] = startGenomes
    var stats = List.empty[(Int, (Double, Double, Double, Double))] // Min, Max, Mean, Var

    while(generation < generations) {
      val generationStartTime = System.nanoTime()
      val output = new io.TextFileOutput(new File("%s/Evo2DSim_run_gen%d.json".format(timeStamp, generation)))
      output.put(genomes.map(x => x._1.toString -> x._2).toJson.toString())
      output.close()


      EnvironmentManager.clean()
      val futureEvaluations =
        ( for(i <- (0 until evaluationPerGeneration).par; id <- (0 until poolSize / groupSize).par ) yield {
              val range = id*groupSize until (id+1)*groupSize
              val e = new BasicEnvironment(timeStep, evaluationSteps, id)
              e.initializeStatic()
              e.initializeAgents(genomes.filterKeys(key => range contains key))
              EnvironmentManager.addEnvironment(e)
              future {
                e.run()
              }
              e.p.future
        } ).seq
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

      val results = for((id, fitness) <- evaluation) yield id -> (fitness / evaluationPerGeneration , genomes(id)._2)
      genomes = nextGeneration(results.toSeq.seq)
      generation +=1
      assert(genomes.size == poolSize)

      val generationFinishedTime = System.nanoTime()
      def timeSpent(t1: Long, t2: Long) = {
        TimeUnit.SECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS)
      }
      def printTime(s: String, t: Long) = println(s.format(t/ 60, t % 60))

      val timeTotalSpent = timeSpent(generationStartTime, generationFinishedTime)
      val timeSetupSpent = timeSpent(generationStartTime, environmentSetupTime)
      val timeSimSpent = timeSpent(environmentSetupTime, simulationFinishedTime)
      val timeEvalSpent = timeSpent(simulationFinishedTime, evaluationFinishedTime)
      val timeNextGenSpent = timeSpent(evaluationFinishedTime, generationFinishedTime)

      printTime("Generation took %d min %d sec in total",timeTotalSpent)
      printTime("Setup of the simulation took %d min %d sec",timeSetupSpent)
      printTime("Simulation took %d min %d sec", timeSimSpent)
      printTime("Evaluation took %d min %d sec", timeEvalSpent)
      printTime("Preparing the next Generation took %d min %d sec", timeNextGenSpent)
      println("Generation %d done, starting next".format(generation))

      stats ::= generation -> collectStats(results.map(_._2._1).toList)

    }
    val output = new io.TextFileOutput(new File("%s/Evo2DSim_stats.json".format(timeStamp)))
    output.put(stats.toJson.toString)
    output.close()
    genomes
  }

  def start() {
    val time = System.nanoTime()
    val genomes = for(id <- (0 until poolSize).par) yield {
      val c = new SBotControllerLinear()
      c.initializeRandom(Random.nextDouble)
      (id, (0.0, c.toGenome))
    }
    val output = new io.TextFileOutput(new File("%s/Evo2DSim_run_final.json".format(timeStamp)))
    output.put(run(Map(genomes.seq: _*)).toJson.toString)
    output.close()
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
    sys.exit()
  }

  def collectStats(results: List[Double]): (Double, Double, Double, Double) = {
    val max = results.max
    val min = results.min
    val mean = results.sum / results.size
    val variance = results.foldLeft(0.0) {(acc, x) => acc + math.pow((x - mean),2)}
    (max, min, mean, variance)
  }
}

