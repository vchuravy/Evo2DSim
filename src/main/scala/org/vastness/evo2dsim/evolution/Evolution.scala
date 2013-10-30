package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}
import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import org.vastness.evo2dsim.gui.EnvironmentManager
import java.util.concurrent.TimeUnit


abstract class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, timeStep: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  def nextGeneration(results: Seq[(Double, Genome)]): IndexedSeq[List[Genome]]

  @tailrec
  private def run(generation: Int, genomes: IndexedSeq[List[Genome]]): IndexedSeq[List[Genome]] = {
    if(generation == generations) {
        genomes
    } else {
      EnvironmentManager.clean()
      val futureEnvironments =
        for(id <- (0 until poolSize / groupSize).par) yield {
          val e = new BasicEnvironment(timeStep, evaluationSteps, id)
          e.initializeStatic()
          e.initializeAgents(groupSize,genomes(e.id))
          EnvironmentManager.addEnvironment(e)
          future {
            e.run()
          }
          e.p.future
        }

      //App.environments = environments.toList

      val envFuture: Future[Seq[Environment]] = Future sequence futureEnvironments.seq
      val env = Await.result(envFuture, Duration.Inf)

      val results = for(e <- env.par; a <- e.agents.par) yield ( a.fitness, a.controller.get.toGenome)

      println("Generation %d done, starting next".format(generation))
      run(generation+1, nextGeneration(results.seq))
      }
  }

  def start() {
    val time = System.nanoTime()
    val genomes = for(id <- 0 until poolSize / groupSize) yield List.empty[Genome]
    run(0, genomes)
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %f min %s sec".format(timeSpent / 60.0f, timeSpent))
  }
}
