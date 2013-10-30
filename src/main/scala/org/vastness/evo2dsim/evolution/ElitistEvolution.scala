package org.vastness.evo2dsim.evolution

import scala.collection.mutable
import scala.util.Random

/**
 * Very stupid test implementation of Elitism
 * @param percent
 * @param poolSize
 * @param groupSize
 * @param evaluationSteps
 * @param generations
 * @param timeStep
 */
class ElitistEvolution(percent: Double, poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, evaluationPerGeneration: Int, timeStep: Int)
  extends Evolution(poolSize, groupSize, evaluationSteps, generations, evaluationPerGeneration, timeStep){

  override def nextGeneration(results: Seq[(Int, (Double, Genome))]): mutable.Map[Int, (Double, Genome)] = {
    val r = results.sortWith(_._2._1 > _._2._1)
    val top = r.view(0,(poolSize*percent).round.toInt)

    mutable.Map(( for(id <- (0 until poolSize).par) yield (id, (0.0, top(Random.nextInt(top.size))._2._2.mutate)) ).seq: _*)
  }
}
