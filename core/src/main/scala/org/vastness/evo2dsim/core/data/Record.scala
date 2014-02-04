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

object Record {
  type Record = Either[Row, Seq[String]]

  def empty: Record = Record(Seq.empty)
  def apply(s: Seq[String]): Record = Right(s)
  def apply(r: Row): Record = Left(r)

  def add(r1: Record, r2: Record) = Record(recordToSeq(r1) ++ recordToSeq(r2))
  def append(r: Record, s: Seq[String]) = Record(recordToSeq(r) ++ s)

  def recordToSeq(r: Record): Seq[String] = r match {
    case Right(s) => s
    case Left(row) => row.toSeq
  }

  def recordToString(r: Record) =  r match {
    case Right(s) => s.mkString(", ")
    case Left(rec) => rec.toString
  }
}
