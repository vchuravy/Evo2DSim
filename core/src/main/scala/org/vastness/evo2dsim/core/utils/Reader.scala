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

import scalax.io.Input
import scalax.io.managed.InputStreamResource
import scalax.file.Path
import de.schlichtherle.truezip.file.{TFileInputStream, TFile}

trait Reader {

  def compressed_?(baseDir: Path, dir: String) = (baseDir / (dir + ".tar.xz")).exists

  def read(baseDir: Path, dir: String, fileName: String): Option[String] =
    if(compressed_?(baseDir, dir)) readCompressed(baseDir, dir, fileName) else readDirect(baseDir, dir, fileName)


  private def readDirect(baseDir: Path, dir: String, fileName: String): Option[String] = {
    val file: Path = baseDir / dir / fileName
    if(file.exists) {
      val input: Input = file.inputStream()
      Some(input.string)
    } else None
  }

  private def readCompressed(baseDir: Path, dir: String, fileName: String): Option[String] = {
    val entry = new TFile((baseDir / (dir + ".tar.xz") / fileName).path)
    if(entry.exists) {
      val input: Input = new InputStreamResource(new TFileInputStream(entry))
      Some(input.string)
    } else None
  }

}
