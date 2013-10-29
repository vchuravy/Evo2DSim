package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import org.vastness.evo2dsim.App


abstract class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, timeStep: Int, simSpeed: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep/simSpeed > 0)
  require(evaluationSteps > 0)

  def nextGeneration(results: Seq[(Double, Genome)]): IndexedSeq[List[Genome]]

  @tailrec
  private def run(generation: Int, genomes: IndexedSeq[List[Genome]]){
    if(generation == 0) {
        return genomes
    } else {
        val environments = for(id <- 0 until poolSize % groupSize)
          yield new BasicEnvironment(timeStep, simSpeed, evaluationSteps, id)

        App.environments = environments.toList

        val futureEnvironments =
          for(e <- environments.par) yield {
            e.initializeStatic()
            e.initializeAgents(groupSize,genomes(e.id))
            e.p.future
          }

        val envFuture: Future[Seq[Environment]] = Future sequence futureEnvironments.seq
        val env = Await.result(envFuture, Duration.Inf)

        val results = for(e <- env; a <- e.agents) yield ( a.fitness, a.controller.get.toGenome)

      run(generation-1,nextGeneration(results))
      }
  }

  def start() {
        val genomes = for(id <- 0 until poolSize % groupSize) yield List.empty[Genome]
        run(generations,genomes)
  }
}
