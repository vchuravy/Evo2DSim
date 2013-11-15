package org.vastness.evo2dsim.evolution

import java.util.concurrent.TimeUnit
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
import scalax.file._
import scalaz.Tree

abstract class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, evaluationPerGeneration: Int, timeStep: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  val now = Calendar.getInstance().getTime
  val dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
  val timeStamp = dateFormat.format(now)

  val dir = (Path("results") resolve "%s".format(timeStamp)).createDirectory()

  def nextGeneration(results: Seq[(Int, (Double, Genome))]): Map[Int, (Double, Genome)]

  private def run(startGenomes: Map[Int, (Double, Genome)]): Map[Int, (Double, Genome)] = {
    val outputStats =  dir resolve "Evo2DSim_stats.csv"
    outputStats.createFile()
    outputStats.append("Generation, Max, Min, Mean, Variance")

    var generation = 0
    var genomes: Map[Int, (Double, Genome)] = startGenomes

    while(generation < generations) {
      val generationStartTime = System.nanoTime()

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

      val output = dir resolve "Evo2DSim_run_gen%d.json".format(generation)
      output.write(results.map(x => x._1.toString -> x._2).toJson.prettyPrint)

      generation +=1
      if(generation < generations) genomes = nextGeneration(results.toSeq.seq)
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

      println("Generation %d done".format(generation))
      printTime("Generation took %d min %d sec in total",timeTotalSpent)
      printTime("Setup of the simulation took %d min %d sec",timeSetupSpent)
      printTime("Simulation took %d min %d sec", timeSimSpent)
      printTime("Evaluation took %d min %d sec", timeEvalSpent)
      printTime("Preparing the next Generation took %d min %d sec", timeNextGenSpent)
      if(generation < generations) println("Starting next generation.")
      else println("We are done here :)")

      val (max, min, mean, variance) = collectStats(results.map(_._2._1).toList)
      outputStats.append("%d, %d, %d, %d, %d \n".format(generation, max, min, mean ,variance))
    }
    genomes
  }

  def start() {
    val time = System.nanoTime()
    val genomes = for(id <- (0 until poolSize).par) yield {
      val c = new SBotControllerLinear()
      c.initializeRandom(Random.nextDouble)
      (id, (0.0, c.toGenome))
    }
    val output = dir resolve "Evo2DSim_gen_tree_.json"
    val result = run(Map(genomes.seq: _*))
    output.write(constructTree(result).drawTree)
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
    sys.exit()
  }

  def collectStats(results: List[Double]): (Double, Double, Double, Double) = {
    val max = results.max
    val min = results.min
    val mean = results.sum / results.size
    val variance = results.foldLeft(0.0) {(acc, x) => acc + math.pow(x - mean,2)} / results.size
    (max, min, mean, variance)
  }

  /**
   * Currently only constructs trees where no crossover happend.
   * @param result
   */
  def constructTree(result: Map[Int, (Double, Genome)]) = {
    val genomes = result.map(_._2._2)
    val history = genomes.map(_.history.reverse)
    _constructTree(-1, history.toList)
  }

  def _constructTree(id: Int, history: List[List[Int]]): Tree[Int] = history match {
    case Nil => Tree.leaf[Int](id)
    case h: List[List[Int]] => Tree.node[Int](id, h.groupBy(_.head).map{ case (k, v) => _constructTree(k, v.map(_.tail))}.toStream)
  }
}

