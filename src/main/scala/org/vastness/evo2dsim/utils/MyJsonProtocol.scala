package org.vastness.evo2dsim.utils

import spray.json._
import org.vastness.evo2dsim.neuro.TransferFunction
import org.vastness.evo2dsim.evolution.{BinaryGenome, Genome}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit object transferFunctionFormat extends JsonFormat[TransferFunction] {
    def write(t: TransferFunction) = JsString(t.name)
    def read(value: JsValue) = TransferFunction.values.find(_.name == value.toString()).get
  }
  implicit object genomeFormat extends JsonFormat[Genome] {
    def write(g: Genome): JsValue = g match {
      case bG: BinaryGenome => binaryGenomeFormat.write(bG)
      case _ => serializationError("Unknown Genome")
    }

    def read(value: JsValue): Genome = value.asJsObject.getFields("name") match {
      case Seq(JsString(name)) if name == "BinaryGenome" =>  binaryGenomeFormat.read(value)
      case _ => deserializationError("Unknown Genome")
    }
  }
  implicit object binaryGenomeFormat extends JsonFormat[BinaryGenome]{
    def write(bG: BinaryGenome) = JsObject(
      "name" -> JsString(bG.name),
      "currentId" -> JsNumber(bG.currentID),
      "weights" -> bG.weightBytes.map(x => x._1.toString -> (x._1, x._2)).toJson,
      "biases" -> bG.biasBytes.map(x => x._1.toString -> x._2).toJson,
      "t_funcs" -> bG.t_funcs.map(x => x._1.toString -> x._2).toJson,
      "mutateBiases" -> JsBoolean(bG.mutateBiases),
      "mutateWeights" -> JsBoolean(bG.mutateWeights),
      "mutateProbability" -> JsNumber(bG.mutateProbability),
      "crossoverProbability" -> JsNumber(bG.crossoverProbability),
      "ancestors" -> JsArray(bG.ancestors.map(JsNumber(_)))
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields("currentId", "weights",
        "biases", "t_funcs", "mutateBiases",
        "mutateWeights", "mutateProbability",
        "crossoverProbability" ,"name") match {
          case Seq(JsNumber(currentId), weights, biases,
                    t_funcs, JsBoolean(mutateBiases),
                    JsBoolean(mutateWeights), JsNumber(mutateProbability),
                    JsNumber(crossoverProbability), JsArray(ancestors), JsString(name)) =>
          BinaryGenome(
            currentId.toInt,
            weights.convertTo[Map[String, ((Int, Int), Byte)]].map(x => x._2),
            biases.convertTo[Map[String, Byte]].map(x => x._1.toInt -> x._2),
            t_funcs.convertTo[Map[String, TransferFunction]].map(x => x._1.toInt -> x._2),
            mutateBiases,
            mutateWeights,
            mutateProbability.toDouble,
            crossoverProbability.toDouble,
            ancestors.map{
              case JsNumber(v) => v.toInt
              case _ => -1},
            name
          )
          case _ => deserializationError("BinaryGenome expected")
      }
    }
  }

}