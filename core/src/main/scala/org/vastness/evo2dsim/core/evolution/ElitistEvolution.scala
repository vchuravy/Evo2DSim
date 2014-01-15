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

package org.vastness.evo2dsim.core.evolution

import scala.util.Random
import org.vastness.evo2dsim.core.evolution.Evolution.Generation

/**
 * Very stupid test implementation of Elitism
 * @param percent
 * @param poolSize
 */
class ElitistEvolution(percent: Double, val poolSize: Int) extends Evolution {
  override def nextGeneration(results: Generation): Generation = {
    val r = results.toSeq.sortWith(_._2._1 > _._2._1)
    val top = r.view(0,(poolSize*percent).round.toInt)

    Map(( for(id <- (0 until poolSize).par) yield (id, (0.0, top(Random.nextInt(top.size))._2._2.mutate)) ).seq: _*)
  }
}
