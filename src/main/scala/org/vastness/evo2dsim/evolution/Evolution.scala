package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}
import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec


abstract class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, timeStep: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  def nextGeneration(results: Seq[(Double, Genome)]): IndexedSeq[List[Genome]]

  @tailrec
  private def run(generation: Int, genomes: IndexedSeq[List[Genome]]){
    if(generation == 0) {
        return genomes
    } else {
      val futureEnvironments =
        for(id <- (0 until poolSize / groupSize).par) yield {
          val e = new BasicEnvironment(timeStep, evaluationSteps, id)
          e.initializeStatic()
          e.initializeAgents(groupSize,genomes(e.id))
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
      run(generation-1, nextGeneration(results.seq))
      }
  }

  def start() {
    val time = System.currentTimeMillis()
    val genomes = for(id <- 0 until poolSize / groupSize) yield List.empty[Genome]
    val timeSpent = (System.currentTimeMillis() - time) / 1000.0 // in seconds
    println("We are done here:" + run(generations,genomes))
    println("Running for: %d min %s sec".format(timeSpent.toLong / 60, timeSpent % 60))
  }
}
