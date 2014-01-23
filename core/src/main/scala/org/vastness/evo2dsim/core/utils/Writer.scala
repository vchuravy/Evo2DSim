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

import de.schlichtherle.truezip.file.{TFileWriter, TFile}
import scalax.file.Path

trait Writer {
  def compress : Boolean

  def write = if(compress) writeCompressed _ else writeDirect _

  private def writeCompressed(baseDir: Path, dir: String, entryName:String, content: String): Unit = {
    if(baseDir.nonExistent) baseDir.createDirectory(createParents = true)

    val file = baseDir / (dir + ".tar.xz")
    val fileName = file.toAbsolute.path
    val entry = new TFile(fileName,entryName)
    val writer = new TFileWriter(entry)
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }

  private def writeDirect(baseDir: Path, dir: String, fileName: String, content: String) {
    val output = baseDir / dir
    if(output.nonExistent) output.createDirectory(createParents = true)
    val file = output / fileName
    file.write(content)
  }

}
