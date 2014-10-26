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

package org.vastness.evo2dsim.core.evolution.genomes.neat

import org.vastness.evo2dsim.core.neuro._
import org.vastness.evo2dsim.core.evolution.genomes.{Node, NodeTag}

case class NEATNode(tag: NodeTag, id: Int, bias: NumberT, transferFunction: TransferFunction, data: String) extends Node {
  type Self = NEATNode
  def mutate(p: Double) = NEATNode(tag, id, bias + (2*random - 1), transferFunction, data)
}

