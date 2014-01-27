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

package org.vastness.evo2dsim.core.simulator

case class AgentID(id: Int = 0, group: Int = 0, generation: Int = 0) {
  override def toString = toString(AgentID.stdSep)
  def toString(sep: Char) = s"$generation$sep$group$sep$id"
}

object AgentID {
  val stdSep = '/'
  def fromString(s: String): AgentID = {
    val a = s.split(stdSep).map(_.toInt)
    require(a.length == 3)
    AgentID(a(2), a(1), a(0))
  }
}
