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
import org.vastness.evo2dsim.core.evolution.genomes.Genome
import scalax.file._
import spray.json._
import org.vastness.evo2dsim.core.utils.MyJsonProtocol._
import org.apache.commons.compress.archivers.sevenz._

import OutputHandler._
import java.io.File

/**
 * An handler for reading the data created by @see OutputHandler.
 * @param dir base dir
 */
class InputHandler(dir: Path) {
  /**
   * Test for compressed data
   * @return
   */
  def compressed_? = (dir / cFileName).exists

  /**
   * Tries to read the generation with the given id
   * @param id generation id
   * @return Option[Generation]
   */
  def readGeneration(id: Int): Option[Generation] = if(compressed_?) readCompressed(id) else read(id)
  
  private def read(id: Int): Option[Generation] = {
    val genFile = dir resolve gFileTemplate.format(id)
    if(genFile.exists) {
      val input: Input = genFile.inputStream()
      Some(string2Generation(input.string))
    } else None
  }

  private def readCompressed(id: Int): Option[Generation] = {
    val cFile = new SevenZFile(new File((dir / cFileName).toURI))
    val content = findAndReadFile(cFile, gFileTemplate.format(id))
    val gen = content map { c => string2Generation(new String(c))}
    cFile.close()
    gen
  }

  private def findAndReadFile(cFile: SevenZFile, fileName: String): Option[Array[Byte]] = {
    var entry = cFile.getNextEntry
    var offset: Int = 0
    while (entry != null && entry.getName != fileName) {
      offset += entry.getSize.toInt
      entry = cFile.getNextEntry
    }
    if (entry == null) None
    else {
        val content =  new Array[Byte](entry.getSize.toInt)
        val l = cFile.read(content)
        if (l != content.length) throw new Exception(s"Read $l bytes instead of ${content.length} bytes")
        Some(content)
    }
  }

  private def string2Generation(in: String): Generation =  {
    val gen = in.asJson.convertTo[Map[String, (Double, Genome)]]
    gen.map (x => x._1.toInt -> x._2)
  }
}
