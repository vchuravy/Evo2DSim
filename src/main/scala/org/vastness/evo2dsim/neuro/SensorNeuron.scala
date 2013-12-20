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

package org.vastness.evo2dsim.neuro

import org.vastness.evo2dsim.evolution.genomes.{Node, NodeTag}

case class SensorNeuron(id: Int, bias: NumberT, t_func: TransferFunction, data: String)(var s_func: () => NumberT = () => zero) extends Neuron {
 val tag = NodeTag.Sensor

  var memory: Boolean = false
  val decay: NumberT = 0.95
  private var lastSensoryInput: NumberT = zero
  override def calcActivity: NumberT = {
    var sensorInput = s_func()
    if(memory) {
      if(sensorInput == zero) {
        sensorInput = lastSensoryInput * decay
      }
      lastSensoryInput = sensorInput
    }
    super.calcActivity + sensorInput
  }
}

object SensorNeuron {
  def apply(n: Node): (() => NumberT) => SensorNeuron = SensorNeuron(n.id, n.bias, n.transferFunction, n.data)
  def apply(n: Node, o: SensorNeuron): SensorNeuron = SensorNeuron(n)(o.s_func)
}
