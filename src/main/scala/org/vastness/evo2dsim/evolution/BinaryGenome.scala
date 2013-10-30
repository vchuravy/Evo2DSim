package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import scala.collection.immutable
import scala.collection.immutable.TreeMap
import scala.util.Random

/**
 * Implements a binary genome in the following format.
 * Every value is stored as 8 bit representing values from -1 to 1
 * Mutation happens via bitflip
 * @param nn The NeuronalNetwork to operate from
 */
class BinaryGenome(nn: NeuronalNetwork, mutateBiases: Boolean = true, mutateWeights: Boolean = true,
                   mutateProbability: Double = 0.01, crossoverProbability: Double = 0.05 ) extends Genome(nn) {

  private val (currentID, neurons, synapses) = nn.serializeNetwork()

  //using TreeMap because it is automatically sorted by key
  private val weights = TreeMap( (for ((id1, id2, w) <- synapses) yield (id1,id2) -> w).toSeq: _*)
  private val t_funcs = TreeMap( (for ((id, _, t_func) <- neurons) yield id -> t_func ).toSeq: _*)
  private val biases  = TreeMap( (for ((id, bias, _ ) <- neurons) yield id -> bias).toSeq: _*)

  private var weightBytes = weights.mapValues(mapToByte)
  private var biasBytes = biases.mapValues(mapToByte)

  protected def bytes = weightBytes.values ++ biasBytes.values

  override def toSerializedNN:(Int,
    immutable.Iterable[(Int, Double, (Double) => Double)],
    immutable.Iterable[(Int,Int,Double)]) = {
      val n = for((id, bias) <- biasBytes.mapValues(mapToDouble)) yield (id, bias, t_funcs(id))
      val s = for(((id1, id2), w) <- weightBytes.mapValues(mapToDouble)) yield (id1, id2, w)
      (currentID, n, s)
  }

  /**
   * Implements a simple bitwise mutation via a xor map
   */
  override def mutateLocal() {
    if(mutateWeights){
      weightBytes = for((id,b) <- weightBytes) yield (id, (b ^ xor()).toByte)
    }

    if(mutateBiases){
      biasBytes = for((id,b) <- biasBytes) yield (id, (b ^ xor()).toByte)
    }
  }

  override def mutate = {
    val newG = new BinaryGenome(nn, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)
    newG.mutateLocal()
    newG
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

  /**
   * Creates a byte that indicates on which point on should flip a byte
   * @return Byte
   */
  private def xor(length:Int = 8, p: Double = mutateProbability): Byte = Integer.parseInt(
    Range(0,length).foldLeft[String]("")(
      (acc, _) =>  if (Random.nextDouble <= p) acc + "1" else acc + "0"
    ), 2).toByte

  /**
   * Map doubles in the range of -1 to 1 to bytes
   * @param value must be in the range of -1 to 1
   * @return signed byte
   */
  private def mapToByte(value: Double): Byte = {
    assert(value.abs <= 1, "Our values are out of range.")
    value match {
      case v if 0 <= v => (v * 127).toByte
      case v if v <  0 => (v * 128).toByte
    }
  }

  /**
   * Map bytes back to doubles in the range of -1 to 1
   * @param value signed Byte
   * @return Double in the range of -1 to 1
   */
  private def mapToDouble(value: Byte): Double = value match{
    case v if 0 <= v => v / 127
    case v if v <  0 => v / 128
  }
}
