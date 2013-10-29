package org.vastness.evo2dsim.evolution

import scala.collection.SortedMap
import scala.util.Random

/**
 * Very stupid test implementation of Elitism
 * @param percent
 * @param poolSize
 * @param groupSize
 * @param evaluationSteps
 * @param generations
 * @param timeStep
 * @param simSpeed
 */
class ElitistEvolution(percent: Double, poolSize: Int, groupSize: Int, evaluationSteps: Int, generations:Int, timeStep: Int, simSpeed: Int)
  extends Evolution(poolSize, groupSize, evaluationSteps, generations, timeStep, simSpeed){

  override def nextGeneration(results: Seq[(Double, Genome)]): IndexedSeq[List[Genome]] = {
    val r = SortedMap(results: _*)
    r.foreach(p => println(p._1))
    val top = r.view(0,(poolSize*percent).round.toInt).toIndexedSeq

    for(id <- 0 until poolSize % groupSize) yield
      (for (i <- 0 until groupSize) yield top(Random.nextInt(top.size))._2.mutate() ).toList

  }
}
