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

package org.vastness.evo2dsim.core.data

import scalax.file.Path

trait Recorder {
  def dir: Path
  def name: String
  def dataHeader: Seq[String]
  def dataRow: () => Seq[Any]

  def step(): Unit
  def write(data: Traversable[Seq[Any]]): Unit

  protected def seqToString(row: Seq[Any]) = if(row.isEmpty) "" else row.tail.foldLeft[String](row.head.toString)(_ + ", " + _)
}

object Recorder {
  def apply(dir: Path, name: String, r: Recordable) = new DirectRecorder(dir, name, r.dataHeader, r.dataRow _)
}