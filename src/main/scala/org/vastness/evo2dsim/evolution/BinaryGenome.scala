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

import org.vastness.evo2dsim.neuro.{TransferFunction, NeuronalNetwork}
import scala.util.Random
import spire.math.Rational

/**
 * Implements a binary genome in the following format.
 * Every value is stored as 8 bit representing values from -1 to 1
 * Mutation happens via bitflip
 */

case class BinaryGenome
  ( currentID: Int, weightBytes: Map[(Int, Int), Byte], biasBytes: Map[Int, Byte],
    t_funcs: Map[Int, TransferFunction],
    mutateBiases: Boolean, mutateWeights: Boolean,
    mutateProbability: Double, crossoverProbability: Double,
    ancestors: List[String], name: String = "BinaryGenome") extends Genome with Binary {

  protected val bytes = weightBytes.values ++ biasBytes.values
  protected var id = -1

  def addId(id: Int) {
    this.id = id
  }

  def history = "G%d_%d".format(ancestors.size, id) :: ancestors

  override def toSerializedNN:(Int,
    Iterable[(Int, Rational, TransferFunction)],
    Iterable[(Int,Int,Rational)]) = {
      val n = for((id, bias) <- biasBytes.mapValues(mapToDouble)) yield (id, bias, t_funcs(id))
      val s = for(((id1, id2), w) <- weightBytes.mapValues(mapToDouble)) yield (id1, id2, w)
      (currentID, n, s)
  }

  /**
   * Implements a simple bitwise mutation via a xor map
   */
  override def mutate = {
    val wB =
      if(mutateWeights)
        for((id,b) <- weightBytes) yield (id, (b ^ xor(p = mutateProbability)).toByte)
      else weightBytes


    val bB =
      if(mutateBiases)
        for((id,b) <- biasBytes) yield (id, (b ^ xor(p = mutateProbability)).toByte)
      else biasBytes

    BinaryGenome(currentID, wB, bB, t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability, history)
  }

  /**
   * implements a crossover at a randomly chosen point with the crossoverProbability.
   * Currently only working for other BinaryGenomes
   * @param other The other genome from which we draw the second half.
   */
  override def crossover(other: Genome): Genome = {
    val (wB, bB) = other match {
      case b: BinaryGenome =>
      {
        val otherG = b.bytes
        if(Random.nextDouble <= crossoverProbability && otherG.size == bytes.size){
          val crossoverPoint = Random.nextInt(otherG.size)
          val newBytes = (bytes.slice(0,crossoverPoint) ++ otherG.slice(crossoverPoint,otherG.size)).toSeq
          var i = 0
          val w = for((id,b) <- weightBytes) yield {
            i+=1
            (id,newBytes(i))
          }
          val b = for((id,b) <- biasBytes) yield {
            i+=1
            (id,newBytes(i))
          }
          (w,b)
        } else (weightBytes, biasBytes)
      }
      case _ => (weightBytes, biasBytes)
    }
    BinaryGenome(currentID, wB, bB, t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability, history ++ other.history)//TODD: history is wrong in this case
  }

  override def toString: String = {
    (
      ( for ((id,b) <- biasBytes) yield "ID: " + id + " Bias: " + b + "\n" ) ++
      ( for ((id,b) <- weightBytes) yield "ID: " + id + " Bias: " + b + "\n")
    ).foldLeft("")((acc,value) => acc + value)
  }
}

object BinaryGenome extends Binary {

  def initialize(nn: NeuronalNetwork, mutateBiases: Boolean = true, mutateWeights: Boolean = true,
            mutateProbability: Double = 0.01, crossoverProbability: Double = 0.05): BinaryGenome = {
    val (currentID, neurons, synapses) = nn.serializeNetwork()
    val weights = Map( (for ((id1, id2, w) <- synapses) yield (id1,id2) -> w).toSeq: _*)
    val t_funcs = Map( (for ((id, _, t_func) <- neurons) yield id -> t_func ).toSeq: _*)
    val biases  = Map( (for ((id, bias, _ ) <- neurons) yield id -> bias).toSeq: _*)

    BinaryGenome(currentID, weights.mapValues(mapToByte), biases.mapValues(mapToByte), t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability, Nil)
  }
}