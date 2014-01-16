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

import org.vastness.evo2dsim.core.data.RecordLevel
import org.vastness.evo2dsim.core.environment.EnvironmentBuilder

case class EvolutionConfig(
                  timeStep: Int = 50,
                  generations: Int = 500,
                  evaluationSteps: Int = 6000,
                  evaluationsPerGeneration: Int = 5,
                  poolSize: Int = 500,
                  groupSize: Int = 10,
                  envConf: String = "0:basic",
                  evolutionAlgorithm: String = "sus",
                  genomeName: String = "NEATGenome",
                  genomeSettings: String = "",
                  propability: Double = 0.1,
                  rLevel: Int = RecordLevel.Nothing.id) {

  require(poolSize % groupSize == 0)
  require(timeStep > 0)
  require(evaluationSteps > 0)

  def envSetup: Seq[(Range, EnvironmentBuilder)] = {
    val conf = envConf.split(';') map {_.split(':').toList } map {
      case x :: List(y) => (x.toInt ,y)
      case _ => throw new IllegalArgumentException(s"Could not parse envConf: $envConf")
    }
    parse(generations, conf)
  }

  def recordingLevel: RecordLevel = RecordLevel.values.find(_.id == rLevel) getOrElse RecordLevel.Nothing

  protected def parse(max: Int, envs: Seq[(Int, String)]) = _parse(max, envs.toList.sortBy(_._1).reverse)
  private   def _parse(next: Int, elems: List[(Int, String)]): List[(Range, EnvironmentBuilder)] = elems match {
    case (gen, name) :: xs => (gen until next, resolve(name)) :: _parse(gen, xs)
    case Nil => List.empty
  }

  private def resolve(name: String) = EnvironmentBuilder.values.find(_.name == name) match {
    case Some(e) => e
    case None => throw new Exception("Could not find: " + name + " in " + EnvironmentBuilder.values)
  }
}