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
import org.apache.commons.compress.archivers.sevenz._
import spray.json._
import org.vastness.evo2dsim.core.utils.MyJsonProtocol._
import scalax.file._
import java.io.File
import OutputHandler._
import org.vastness.evo2dsim.core.evolution.EvolutionConfig

/**
 * Allows for either compressed or uncompressed output of generations
 * @param dir base dir
 * @param compress flag
 */
class OutputHandler(dir: Path, compress: Boolean) {
  val gFile: Option[SevenZOutputFile] = if(compress) Some(new SevenZOutputFile(new File((dir / cFileName).toURI))) else None

  /**
   * Given a generation write it
   * @param id generation number
   * @param gen generation
   */
  def writeGeneration(id: Int, gen: Generation) = if(compress) writeCompressed(id, gen) else write(id, gen)
  def writeEvolutionConfig(config: EvolutionConfig) = {
    val s = config.toJson.prettyPrint
    val file = dir / configFileName
    file.write(s)
  }

  def finish() {
    gFile map {_.finish()}
  }
  
  private def writeCompressed(id: Int, gen: Generation): Unit = gFile match {
    case Some(cFile) =>
      val o7 = new SevenZArchiveEntry()
      o7.setName(gFileTemplate.format(id))

      val output = gen2JSONString(gen).getBytes
      o7.setSize(output.size)

      cFile.putArchiveEntry(o7)
      cFile.write(output)
      cFile.closeArchiveEntry()

    case None => throw new Exception("Trying to write a compressed File, when there is none.")
  }

  private def write(id: Int, gen: Generation) {
    val file = dir / gFileTemplate.format(dir)
    val output = gen2JSONString(gen)
    file.write(output)
  }

  private def gen2JSONString(gen: Generation) = gen.toJson.prettyPrint
}

object OutputHandler {
  val cFileName = "generations.7z"
  val gFileTemplate = "Gen_%04d.json"
  val configFileName = "Config.json"
}


