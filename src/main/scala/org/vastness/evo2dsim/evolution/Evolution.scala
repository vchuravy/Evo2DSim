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

import java.util.concurrent.TimeUnit
import spray.json._
import org.vastness.evo2dsim.utils.MyJsonProtocol._

import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.vastness.evo2dsim.gui.EnvironmentManager
import org.vastness.evo2dsim.environment.{BasicRandomEnvironment, Environment}
import org.vastness.evo2dsim.teem.enki.sbot.SBotControllerLinear
import scala.util.Random
import java.util.Calendar
import java.text.SimpleDateFormat
import scalax.file._
import scala.{Double, Int}
import scalaz.{TreeLoc, Tree}
import scala.annotation.tailrec
import scala.collection.immutable.Stream.Empty

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
    val outputStats =  dir resolve "Stats.csv"
    outputStats.createFile()
    outputStats.append("Generation, Max, Min, Mean, Variance \n")

    var generation = 0
    var genomes: Map[Int, (Double, Genome)] = startGenomes

    while(generation < generations) {
      val generationStartTime = System.nanoTime()

      EnvironmentManager.clean()

      val futureEvaluations =  groupEvaluations(genomes.toList)
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

      val output = dir resolve "Gen_%04d.json".format(generation)
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
      outputStats.append("%d, %f, %f, %f, %f \n".format(generation, max, min, mean ,variance))
    }
    genomes
  }


  def groupEvaluations(genomes: List[(Int, (Double, Genome))]): Seq[Future[Environment]] = {
    val gs = genomes.sortBy(_._1).grouped(groupSize).toSeq
    ( for (g <- gs.par) yield {
      for (i <- 0 until evaluationPerGeneration) yield {
        val e = new BasicRandomEnvironment(timeStep, evaluationSteps)
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

  def start() {
    val time = System.nanoTime()
    val genomes = for(id <- (0 until poolSize).par) yield {
      val c = new SBotControllerLinear()
      c.initializeRandom(Random.nextDouble)
      (id, (0.0, c.toGenome))
    }
    val result = run(Map(genomes.seq: _*))
    writeGraphViz(result)
    writeNewickTree(result)
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

  def writeGraphViz(genomes: Map[Int, (Double, Genome)]){
    val result = genomes.map(_._2._2)
    val history = result.map(_.history.reverse).toList
    val dot = for(h <- history) yield {
      h.foldLeft("ROOT")(_ + " -> " + _) + ";"
    }
    val o = "digraph Tree {" :: dot
    val output = dir / "Tree.gv"
    output.writeStrings(o, "\n")
    output.append("}")
  }

  def writeNewickTree(genomes: Map[Int, (Double, Genome)]){
    val tree = constructTree(genomes)
    val newick = createNewickTree(tree) + ";"
    val output = dir / "Tree.nh"
    output.write(newick)

  }

  /**
   * Currently only constructs trees where no crossover happend.
   * @param result
   */
  def constructTree(result: Map[Int, (Double, Genome)]): Tree[String] = {
    val genomes = result.map(_._2._2)
    val history = genomes.map(_.history.reverse).toList
    _constructTree(Tree.leaf[String]("ROOT"),history)
  }

  def _constructTree(tree: Tree[String], history: List[List[String]]):Tree[String] = {
    var t = tree.loc
    for(path <- history) {
      t = createTreeFromPath(path, t.root)
    }
    t.toTree
  }

  @tailrec
  private def createTreeFromPath(path: List[String], t: TreeLoc[String]): TreeLoc[String] = path match {
    case x :: xs => {
      t.findChild(_.rootLabel == x) match {
        case Some(child) => createTreeFromPath(xs, child)
        case None => {
          val newT = t.insertDownFirst(Tree.leaf(x))
          createTreeFromPath(xs, newT)
        }
      }
    }
    case _ => t
  }

  def createNewickTree(tree: Tree[String]):String = tree.subForest match {
    case Empty => tree.rootLabel
    case forest: Stream[Tree[String]] =>
      forest.foldLeft("(")((acc, tree) => acc + createNewickTree(tree) + ",").dropRight(1) + ")"+tree.rootLabel

  }
}

