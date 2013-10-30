package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}
import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.vastness.evo2dsim.gui.EnvironmentManager
import java.util.concurrent.TimeUnit
import scala.collection.Map
import org.vastness.evo2dsim.teem.enki.sbot.{SBot, SBotControllerLinear}
import org.vastness.evo2dsim.simulator.Simulator
import org.jbox2d.common.Vec2


abstract class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, timeStep: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  def nextGeneration(results: Seq[(Int, (Double, Genome))]): Map[Int, (Double, Genome)]

  private def run( startGenomes: Map[Int, (Double, Genome)]): List[Map[Int, (Double, Genome)]] = {
    var generation = 0
    var genomes = List(startGenomes)

    while(generation < generations) {
      EnvironmentManager.clean()
      val futureEnvironments =
        for(id <- (0 until poolSize / groupSize).par) yield {
          val range = id*groupSize until (id+1)*groupSize
          val e = new BasicEnvironment(timeStep, evaluationSteps, id)
          e.initializeStatic()
          e.initializeAgents(genomes.head.filterKeys(key => range contains key))
          EnvironmentManager.addEnvironment(e)
          future {
            e.run()
          }
          e.p.future
        }

        val envFuture: Future[Seq[Environment]] = Future sequence futureEnvironments.seq
        val env = Await.result(envFuture, Duration.Inf)

        val results = for(e <- env.par;(id, a) <- e.agents) yield {
          val (_, genome) = genomes.head(id)
          (id,(a.fitness, genome))
        }

      println("Generation %d done, starting next".format(generation))
      genomes ::= nextGeneration(results.seq)
      generation +=1
    }
    genomes
  }

  def start() {
    val time = System.nanoTime()
    //TODO: We just need an genome with the correct neuron ids, should be that hard.
    val sim = new Simulator(0)
    val sBot = new SBot(-1,new Vec2(0,0), sim)

    val genomes = for(id <- 0 until poolSize) yield {
      val c = new SBotControllerLinear(sBot)
      c.initializeRandom()
      (id, (0.0, c.toGenome))
    }
    run(Map(genomes: _*))
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
  }
}
