package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import scala.collection.immutable
import scala.collection.immutable.{SortedMap, TreeMap}
import scala.util.Random

/**
 * Implements a binary genome in the following format.
 * Every value is stored as 8 bit representing values from -1 to 1
 * Mutation happens via bitflip
 */

class BinaryGenome private
  ( t_funcs: TreeMap[Int, (Double) => Double],
    mutateBiases: Boolean, mutateWeights: Boolean,
    mutateProbability: Double, crossoverProbability: Double) extends Genome with Binary {

  private var currentID = -1
  private var weightBytes = SortedMap[(Int, Int), Byte]()
  private var biasBytes = SortedMap[Int, Byte]()

  //using TreeMap because it is automatically sorted by key

  protected def bytes = weightBytes.values ++ biasBytes.values

  override def toSerializedNN:(Int,
    immutable.Iterable[(Int, Double, (Double) => Double)],
    immutable.Iterable[(Int,Int,Double)]) = {
      val n = for((id, bias) <- biasBytes.mapValues(mapToDouble)) yield (id, bias, t_funcs(id))
      val s = for(((id1, id2), w) <- weightBytes.mapValues(mapToDouble)) yield (id1, id2, w)
      (currentID, n, s)
  }

  def copy : BinaryGenome =
    BinaryGenome(currentID, weightBytes, biasBytes, t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)

  /**
   * Implements a simple bitwise mutation via a xor map
   */
  override def mutateLocal() {
    if(mutateWeights){
      weightBytes = for((id,b) <- weightBytes) yield (id, (b ^ xor(p = mutateProbability)).toByte)
    }

    if(mutateBiases){
      biasBytes = for((id,b) <- biasBytes) yield (id, (b ^ xor(p = mutateProbability)).toByte)
    }
  }

  override def mutate = {
    val newGenome = this.copy
    newGenome.mutateLocal()
    newGenome
  }

  /**
   * implements a crossover at a randomly chosen point with the crossoverProbability.
   * Currently only working for other BinaryGenomes
   * @param other The other genome from which we draw the second half.
   */
  override def crossover(other: Genome): Genome = {
    other match {
      case b: BinaryGenome =>
      {
        val otherG = b.bytes
        if(Random.nextDouble <= crossoverProbability && otherG.size == bytes.size){
          val crossoverPoint = Random.nextInt(otherG.size)
          val newBytes = (bytes.slice(0,crossoverPoint) ++ otherG.slice(crossoverPoint,otherG.size)).toSeq
          var i = 0
          weightBytes = for((id,b) <- weightBytes) yield {
            i+=1
            (id,newBytes(i))
          }
          biasBytes = for((id,b) <- biasBytes) yield {
            i+=1
            (id,newBytes(i))
          }
        }
      }
    }
    return this
  }

  override def toString: String = {
    (
      ( for ((id,b) <- biasBytes) yield "ID: " + id + " Bias: " + b + "\n" ) ++
      ( for ((id,b) <- weightBytes) yield "ID: " + id + " Bias: " + b + "\n")
    ).foldLeft("")((acc,value) => acc + value)
  }
}

object BinaryGenome extends Binary {

  def apply(nn: NeuronalNetwork, mutateBiases: Boolean = true, mutateWeights: Boolean = true,
            mutateProbability: Double = 0.01, crossoverProbability: Double = 0.05): BinaryGenome = {
    val (currentID, neurons, synapses) = nn.serializeNetwork()
    val weights = TreeMap( (for ((id1, id2, w) <- synapses) yield (id1,id2) -> w).toSeq: _*)
    val t_funcs = TreeMap( (for ((id, _, t_func) <- neurons) yield id -> t_func ).toSeq: _*)
    val biases  = TreeMap( (for ((id, bias, _ ) <- neurons) yield id -> bias).toSeq: _*)

    BinaryGenome(currentID, weights.mapValues(mapToByte), biases.mapValues(mapToByte), t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)
  }

  def apply(currentID: Int, weightBytes: SortedMap[(Int, Int), Byte],
            biasBytes: SortedMap[Int, Byte], t_funcs: TreeMap[Int, (Double) => Double],
            mutateBiases: Boolean, mutateWeights: Boolean,
            mutateProbability: Double, crossoverProbability: Double) : BinaryGenome = {

    val binaryGenome = new BinaryGenome(t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)

    binaryGenome.currentID = currentID
    binaryGenome.weightBytes = weightBytes
    binaryGenome.biasBytes = biasBytes

    binaryGenome
  }
}
