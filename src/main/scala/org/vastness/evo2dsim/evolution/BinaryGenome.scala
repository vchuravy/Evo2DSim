package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.neuro.NeuronalNetwork
import scala.collection.immutable.{Iterable, TreeMap}
import scala.util.Random

/**
 * Implements a binary genome in the following format.
 * Every value is stored as 8 bit representing values from -1 to 1
 * Mutation happens via bitflip
 */

case class BinaryGenome
  ( currentID: Int, weightBytes: Map[(Int, Int), Byte], biasBytes: Map[Int, Byte],
    t_funcs: Map[Int, (Double) => Double],
    mutateBiases: Boolean, mutateWeights: Boolean,
    mutateProbability: Double, crossoverProbability: Double) extends Genome with Binary {

  protected def bytes = weightBytes.values ++ biasBytes.values

  override def toSerializedNN:(Int,
    Iterable[(Int, Double, (Double) => Double)],
    Iterable[(Int,Int,Double)]) = {
      val n = for((id, bias) <- biasBytes.mapValues(mapToDouble)) yield (id, bias, t_funcs(id))
      val s = for(((id1, id2), w) <- weightBytes.mapValues(mapToDouble)) yield (id1, id2, w)
      (currentID, n, s)
  }

  def copy : BinaryGenome =
    BinaryGenome(currentID, weightBytes, biasBytes, t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)

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

    BinaryGenome(currentID, wB, bB, t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)
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
    BinaryGenome(currentID, wB, bB, t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)
  }

  override def toString: String = {
    (
      ( for ((id,b) <- biasBytes) yield "ID: " + id + " Bias: " + b + "\n" ) ++
      ( for ((id,b) <- weightBytes) yield "ID: " + id + " Bias: " + b + "\n")
    ).foldLeft("")((acc,value) => acc + value)
  }

  /** override def GenomeCodecJson: CodecJson[BinaryGenome] =
    CodecJson(
      (g: BinaryGenome) =>
        ("currentID" := g.currentID) ->:
        ("weightBytes" := g.weightBytes) ->:
        ("biasBytes" := g.biasBytes) ->:
        ("t_funcs" := g.t_funcs) ->:
        ("mutateWeights" := g.mutateWeights) ->:
        ("mutateBiases" := g.mutateBiases) ->:
        ("mutateProbability" := g.mutateProbability) ->:
        ("crossoverProbability" := g.crossoverProbability) ->:
        jEmptyObject,
      con => for {
        c <- (con --\ "currentID").as[Int]
        wB <- (con --\ "weightBytes").as[Map[(Int, Int), Byte]]
        bB <- (con --\ "biasBytes").as[Map[Int, Byte]]
        t_f <- (con --\ "t_funcs").as[TreeMap[Int, (Double) => Double]]
        mW <- (con --\ "mutateWeights").as[Boolean]
        mB <- (con --\ "mutateBiases").as[Boolean]
        mP <- (con --\ "mutateProbability").as[Double]
        cP <- (con --\ "crossoverProbability").as[Double]
      } yield BinaryGenome(c, wB, bB, t_f, mW, mB, mP, cP)
    )
    //Argonaut.casecodec8(BinaryGenome.apply, BinaryGenome.unapply)("currentID", "weightBytes", "biasBytes", "t_funcs",
    //  "mutateWeights", "mutateBiases", "mutateProbability", "crossoverProbability")
    */
}

object BinaryGenome extends Binary {

  def initialize(nn: NeuronalNetwork, mutateBiases: Boolean = true, mutateWeights: Boolean = true,
            mutateProbability: Double = 0.01, crossoverProbability: Double = 0.05): BinaryGenome = {
    val (currentID, neurons, synapses) = nn.serializeNetwork()
    val weights = TreeMap( (for ((id1, id2, w) <- synapses) yield (id1,id2) -> w).toSeq: _*)
    val t_funcs = TreeMap( (for ((id, _, t_func) <- neurons) yield id -> t_func ).toSeq: _*)
    val biases  = TreeMap( (for ((id, bias, _ ) <- neurons) yield id -> bias).toSeq: _*)

    BinaryGenome(currentID, weights.mapValues(mapToByte), biases.mapValues(mapToByte), t_funcs, mutateBiases, mutateWeights, mutateProbability, crossoverProbability)
  }
}
