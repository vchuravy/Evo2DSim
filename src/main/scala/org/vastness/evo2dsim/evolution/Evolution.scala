package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}
import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
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

  @tailrec
  private def run(generation: Int, genomes: Map[Int, (Double, Genome)]): Map[Int, (Double, Genome)] = {
    if(generation == generations) {
        genomes
    } else {
      EnvironmentManager.clean()
      val futureEnvironments =
        for(id <- (0 until poolSize / groupSize).par) yield {
          val range = id*groupSize until (id+1)*groupSize
          val e = new BasicEnvironment(timeStep, evaluationSteps, id)
          e.initializeStatic()
          e.initializeAgents(genomes.filterKeys(key => range contains key))
          EnvironmentManager.addEnvironment(e)
          future {
            e.run()
          }
          e.p.future
        }

      val envFuture: Future[Seq[Environment]] = Future sequence futureEnvironments.seq
      val env = Await.result(envFuture, Duration.Inf)

      val results = for(e <- env.par;(id, a) <- e.agents) yield {
        val (_, genome) = genomes(id)
        (id,(a.fitness, genome))
      }

      println("Generation %d done, starting next".format(generation))
      run(generation+1, nextGeneration(results.seq))
      }
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
    run(0, Map(genomes: _*))
    val timeSpent = TimeUnit.SECONDS.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS)
    println("We are done here:")
    println("Running for: %d min %s sec".format(timeSpent / 60, timeSpent % 60))
  }
}
