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

package org.vastness.evo2dsim.core.utils

import spray.json._
import org.vastness.evo2dsim.core.neuro.TransferFunction
import org.vastness.evo2dsim.core.evolution.genomes.{NodeTag, Genome}
import org.vastness.evo2dsim.core.evolution.genomes.byte._
import org.vastness.evo2dsim.core.evolution.genomes.neat._
import org.vastness.evo2dsim.core.evolution.genomes.standard._
import org.vastness.evo2dsim.core.evolution.EvolutionConfig
import org.vastness.evo2dsim.core.simulator.AgentID


object MyJsonProtocol extends DefaultJsonProtocol {
  implicit object transferFunctionFormat extends JsonFormat[TransferFunction] {
    def write(t: TransferFunction) = JsString(t.name)
    def read(value: JsValue) = value match {
      case JsString(name) => TransferFunction.values.find(_.name == name).get
      case _ => deserializationError("Got: " + value + " expected JsString")
    }
  }

  implicit object nodeTagFormat extends JsonFormat[NodeTag] {
    def write(nT: NodeTag) = JsString(nT.name)
    def read(value: JsValue) = value match {
      case JsString(name) => NodeTag.values.find(_.name == name).get
      case _ => deserializationError("Got: " + value + " expected JsString")
    }
  }

  implicit val neatNodeFormat = jsonFormat5(NEATNode)
  implicit val neatConnectionFormat = jsonFormat5(NEATConnection)
  implicit object neatGenomeFormat extends JsonFormat[NEATGenome] {
    def write(nG: NEATGenome) = JsObject(
      "nodes" -> nG.nodes.toJson,
      "connections" -> nG.connections.toJson,
      "stdTFunc" -> nG.nm.standardTransferFunction.toJson,
      "p" -> JsNumber(nG.nm.probability)
    )

    def read(value: JsValue) =
      value.asJsObject.getFields("nodes", "connections", "stdTFunc", "p") match {
        case Seq(nodes, connections, stdTFunc, JsNumber(p)) =>
          val n = nodes.convertTo[Set[NEATNode]]
          val c = connections.convertTo[Set[NEATConnection]]
          val sTF = stdTFunc.convertTo[TransferFunction]
          val nm = new NEATEvolutionManager(p.toDouble, sTF)
          NEATGenome(n,c,nm)
        case _ => deserializationError("Got: " + value + " expected NEATGenome")
      }
  }

  implicit val byteNodeFormat = jsonFormat5(ByteNode)
  implicit val byteConnectionFormat = jsonFormat3(ByteConnection)
  implicit object byteGenomeFormat extends JsonFormat[ByteGenome] {
    def write(bG: ByteGenome) = JsObject(
      "nodes" -> bG.nodes.toJson,
      "connections" -> bG.connections.toJson,
      "stdTFunc" -> bG.em.standardTransferFunction.toJson,
      "p" -> JsNumber(bG.em.probability)
    )

    def read(value: JsValue) =
      value.asJsObject.getFields("nodes", "connections", "stdTFunc", "p") match {
        case Seq(nodes, connections, stdTFunc, JsNumber(p)) =>
          val n = nodes.convertTo[Set[ByteNode]]
          val c = connections.convertTo[Set[ByteConnection]]
          val sTF = stdTFunc.convertTo[TransferFunction]
          val em = new ByteEvolutionManager(p.toDouble, sTF)
          ByteGenome(n,c,em)
        case _ => deserializationError("Got: " + value + " expected ByteGenome")
      }
  }

  implicit val stdNodeFormat = jsonFormat5(STDNode)
  implicit val stdConnectionFormat = jsonFormat3(STDConnection)
  implicit object stdEvolutionManagerFormat extends JsonFormat[STDEvolutionManager] {
    def write(em: STDEvolutionManager) = JsObject(
      "p" -> JsNumber(em.probability),
      "t_func" -> em.standardTransferFunction.toJson,
      "settings" -> JsString(s"${em.recurrent}:${em.numberOfHiddenNeurons}")
    )

    def read(value: JsValue) = value.asJsObject.getFields("p", "t_func", "settings") match {
      case Seq(JsNumber(p), t_func, JsString(settings)) =>
        STDEvolutionManager(p.toDouble, t_func.convertTo[TransferFunction], settings)
    }
  }
  implicit val stdGenomeFormat = jsonFormat(STDGenome, "nodes", "connections", "em")

  implicit object genomeFormat extends JsonFormat[Genome] {
    def write(g: Genome) = {
      val jsG = g match {
        case bG: ByteGenome => bG.toJson
        case nG: NEATGenome => nG.toJson
        case sG: STDGenome => sG.toJson
      }
      JsObject("name" -> JsString(g.name), "data" -> jsG)
    }

    def read(value: JsValue) = value.asJsObject.getFields("name", "data") match {
      case Seq(JsString("ByteGenome"), data) => data.convertTo[ByteGenome]
      case Seq(JsString("NEATGenome"), data) => data.convertTo[NEATGenome]
      case Seq(JsString("STDGenome"), data) => data.convertTo[STDGenome]
      case _ => deserializationError("Got: " + value + " expected Genome")
    }
  }

  implicit val evolutionConfigFormat = jsonFormat12(EvolutionConfig)
  implicit object agentIDFormat extends JsonFormat[AgentID] {
    def write(a: AgentID) = JsString(a.toString)
    def read(v: JsValue) = v match {
      case JsString(value) => AgentID.fromString(value)
      case _ => deserializationError("Got: " + v + " expected AgentID")
    }
  }

}