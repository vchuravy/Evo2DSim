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

package org.vastness.evo2dsim.core.evolution.genomes


trait Genome {
  type Self <: Genome
  type SelfNode <: Node
  type SelfConnection <: Connection

  def name: String

  def mutate: Self
  def crossover(other: Self): Self

  def nodes: Set[SelfNode]
  def connections: Set[SelfConnection]

  def distance(other: Genome): Double

  def connectionMap: Map[(Int, Int), SelfConnection] = connections.map(c => ((c.from, c.to), c)).toMap
  def nodesMap: Map[Int, SelfNode] = nodes.map(n => (n.id, n)).toMap

  def findNode(id: Int): Option[SelfNode] = nodes.find(_.id == id)

  /**
   * zips over two maps. !expects the same keys to be available
   */
  def zipper[A, B](a: Map[A, B], b: Map[A,B]): Iterable[(B, B)] = {
    for (key <- a.keys ++ b.keys)
      yield (a(key), b(key))
  }

}
