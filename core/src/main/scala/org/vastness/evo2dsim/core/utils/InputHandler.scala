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

package org.vastness.evo2dsim.core.utils

import org.vastness.evo2dsim.core.evolution.Evolution.Generation
import scalax.io.Input
import scalax.file._
import spray.json._
import org.vastness.evo2dsim.core.utils.MyJsonProtocol._

import OutputHandler._
import org.vastness.evo2dsim.core.evolution.EvolutionConfig

/**
 * An handler for reading the data created by @see OutputHandler.
 * @param dir base dir
 */
class InputHandler(dir: Path) extends Reader {

  /**
   * Tries to read the generation with the given id
   * @param id generation id
   * @return Option[Generation]
   */
  def readGeneration(id: Int): Option[Generation] = {
    val content = read(dir, gDirName, gFileTemplate(id))
    content map string2Generation
  }

  def readEvolutionConfig: Option[EvolutionConfig] = {
    val cFile = dir / configFileName
    if(cFile.exists) {
      val input: Input = cFile.inputStream()
      Some(input.string.asJson.convertTo[EvolutionConfig])
    } else None
  }

  private def string2Generation(in: String): Generation = in.asJson.convertTo[Generation]
}
