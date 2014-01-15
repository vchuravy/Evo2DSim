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

import org.vastness.evo2dsim.core.evolution.genomes.{Node, NodeTag}

case class MotorNeuron(id: Int, bias: NumberT, t_func: TransferFunction, data: String)(var m_func: (NumberT) => Unit = (_) => {}) extends Neuron {
  override val tag = NodeTag.Motor
  override def step() {
    super.step()
    m_func(output)
  }
}

object MotorNeuron {
  def apply(n: Node): ((NumberT) => Unit) => MotorNeuron = MotorNeuron(n.id, n.bias, n.transferFunction, n.data)
  def apply(n: Node, o: MotorNeuron): MotorNeuron = MotorNeuron(n)(o.m_func)
}
