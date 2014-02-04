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

package org.vastness.evo2dsim.core.neuro

import org.vastness.evo2dsim.core.evolution.genomes.{NodeTag, Node, Genome}
import org.vastness.evo2dsim.core.data.{Recordable, Record}

case class NeuronalNetwork(synapses: Set[Synapse], neurons: Set[Neuron]) extends Recordable {
  def step() { //Order matters
    neurons foreach {n => n.step()}
    synapses foreach { s => s.step() }
  }

  def dataHeader = neurons.foldLeft(Seq.empty[String])(_ ++ _.dataHeader)
  def dataRow = neurons.foldLeft(Record.empty)((acc, obj) => Record.add(acc, obj.dataRow))

  def toDot: String = {
    val header = "digraph NeuronalNetwork { \n"
    val nodes = for(n <- neurons) yield {
      "%d [label=\"%s\"]; \n".format(n.id, n.data)
    }
    val connections = for(s <- synapses) yield {
      val from = s.input.id
      val to = s.output.id
      "%d -> %d [label=\"%f.2\"]; \n".format(from, to, s.weight)
    }
    val end = " } \n"

    header + nodes + connections + end
  }
}

object NeuronalNetwork {
  def apply(inputs: Set[SensorNeuron], outputs: Set[MotorNeuron], genome: Genome): NeuronalNetwork = {
    val taggedNodes = genome.nodes.groupBy(_.tag)
    val sensorNeurons = for(i <- inputs) yield {
      val n = taggedNodes(NodeTag.Sensor).find(_.data == i.data).get
      SensorNeuron(n)(i.s_func)
    }
    val motorNeurons = for(o <- outputs) yield {
      val n = taggedNodes(NodeTag.Motor).find(_.data == o.data).get
      MotorNeuron(n)(o.m_func)
    }

    val hiddenNeurons = for(n <- taggedNodes.getOrElse(NodeTag.Hidden, Set.empty)) yield HiddenNeuron(n)
    val neurons: Set[Neuron] = hiddenNeurons ++ sensorNeurons ++ motorNeurons

    def findNeuron(node: Node) = neurons.find(_.id == node.id) match {
      case Some(n) => n
      case None => throw new Exception(s"Could not find neuron from node: $node")
    }

    val synapses = for(c <- genome.connections) yield new Synapse(findNeuron(c.from), findNeuron(c.to), c.weight)
    val inSynapses = synapses.groupBy(_.output.id)
    neurons foreach {n => n.inputSynapses = n.inputSynapses ++ inSynapses.getOrElse(n.id, Set.empty).toVector}
    NeuronalNetwork(synapses, neurons)
  }
}