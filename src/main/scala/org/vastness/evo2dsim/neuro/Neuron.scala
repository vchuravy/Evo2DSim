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

import spire.implicits._
import spire.math._

class Neuron(var bias: Rational, var t_func: TransferFunction) {
  var id = -1

  var inputSynapses =  Set.empty[Synapse]

  private var activity: Rational = 0.0
  def output: Rational = t_func(activity)

  protected def calcActivity: Rational =
    inputSynapses.map(_.value).qsum + bias

  def step() {
    activity = calcActivity
  }

  def addInput(s: Synapse){
    inputSynapses += s
  }

  def removeInput(s: Synapse){
    inputSynapses -= s
  }

  override def toString = id.toString
}


