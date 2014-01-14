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

package org.vastness.evo2dsim.data

import org.vastness.utils.Enum

sealed trait RecordLevel {
  def id: Int
  def record(other: RecordLevel): Boolean = other.id <= id
}

object RecordLevel extends Enum[RecordLevel] {
  case object Everything extends RecordLevel { val id = 0 }
  case object Controller extends RecordLevel { val id = 1 }
  case object Agents extends RecordLevel { val id = 2 }
  case object Nothing extends RecordLevel {
    val id = 3
    override def record(other: RecordLevel) = false
  }
}


