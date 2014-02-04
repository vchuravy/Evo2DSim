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

import org.vastness.evo2dsim.core.evolution.genomes.NodeTag
import spire.syntax.cfor._
import org.vastness.evo2dsim.core.data._, Record._

trait Neuron extends Product with Serializable with Recordable{
  def id: Int
  def bias: NumberT
  var inputSynapses: Vector[Synapse] = Vector.empty
  def t_func: TransferFunction
  def tag: NodeTag
  def data: String

  protected var activity: NumberT = zero
  def output: NumberT = t_func(activity)

  protected def calcActivity: NumberT = sumInputs(inputSynapses) + bias

  private def sumInputs(inputs: IndexedSeq[Synapse]): NumberT = {
    var sum = zero
    cfor(0)(_ < inputSynapses.length, _ +1) { i =>
      sum += inputSynapses(i).value
    }
    sum
  }


  def step() {
    activity = calcActivity
  }

  override def toString = id.toString
  override def dataHeader = Seq(h("activity"), h("output"))
  override def dataRow = Record(NeuronRow(activity, output))

  protected def h(s: String) = s"${data}_$s"
}

case class NeuronRow(activity: NumberT, output: NumberT) extends Row