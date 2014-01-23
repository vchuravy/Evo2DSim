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
import spray.json._
import org.vastness.evo2dsim.core.utils.MyJsonProtocol._
import scalax.file._
import OutputHandler._
import org.vastness.evo2dsim.core.evolution.EvolutionConfig
import de.schlichtherle.truezip.file.{TFileWriter, TFile}

/**
 * Allows for either compressed or uncompressed output of generations
 * @param dir base dir
 * @param compress flag
 */
class OutputHandler(dir: Path, val compress: Boolean) extends Writer {
  /**
   * Given a generation write it
   * @param id generation number
   * @param gen generation
   */
  def writeGeneration(id: Int, gen: Generation) = {
    write(dir, gDirName, gFileTemplate(id), gen2JSONString(gen))
  }

  def writeEvolutionConfig(config: EvolutionConfig) = {
    val s = config.toJson.prettyPrint
    val file = dir / configFileName
    file.write(s)
  }


  private def gen2JSONString(gen: Generation) = gen.toJson.prettyPrint
}

object OutputHandler {
  val gDirName = "generations"
  def gFileTemplate(id: Int) = s"Gen_$id.json"
  val configFileName = "Config.json"
}


