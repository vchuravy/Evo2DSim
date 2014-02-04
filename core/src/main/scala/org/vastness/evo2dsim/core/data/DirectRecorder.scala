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

import scalax.file.Path
import org.vastness.evo2dsim.core.data.Record._

class DirectRecorder(val dir: Path, val name: String, val dataHeader: Seq[String], val dataRecord: () => Record = () => Record.empty, append: Boolean = true) extends Recorder{
    private val output: Path =  dir resolve s"$name.csv"
    if(!append) output.deleteIfExists()
    if(output.nonExistent) {
      output.createFile()
      writeln(headerToString(dataHeader))
    }

    def step() = writeRecord(dataRecord())
    def write(data: Seq[Record]) {
      for(row <- data) {
        writeRecord(row)
      }
    }

    protected def writeln(s: String) = output.append(s + "\n")
    protected def writeRecord(row: Record) = writeln(recordToString(row))
}
