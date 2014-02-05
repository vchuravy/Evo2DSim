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

/*
 * This file is part of Evo2DSimAnalyzer.
 *
 * Evo2DSimAnalyzer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSimAnalyzer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSimAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vastness.evo2dsim.analyzer.gui

import scala.swing._
import java.awt.event.{MouseEvent, MouseAdapter}

class TableDialog(owner: Window, val table: Table) extends Dialog(owner) {
  private var result = Dialog.Result.Closed

  setLocationRelativeTo(owner)

  table.peer.setAutoCreateRowSorter(true)

  val ok = new Button(Action("Ok") {
    result = Dialog.Result.Ok
    peer.setVisible(false)
  })
  val cancel = new Button(Action("Cancel") {
    result = Dialog.Result.Cancel
    peer.setVisible(false)
  })

  def tablePane: Component = new ScrollPane() {
    contents = table
  }

  val bP = new BorderPanel {
    import BorderPanel._

    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += ok
      contents += cancel
    }

    add(tablePane, Position.North)
    add(buttonPanel, Position.South)
  }

  contents = bP

  def showTableDialog() = {
    this.modal = true
    this.pack()
    this.visible = true
  }

  def selectedValue: Int = {
    val selected = table.selection.rows
    // if (selected.tail.nonEmpty) println("Warning: Discarded values from selection")
    selected.headOption.getOrElse(0)
  }
}

object TableDialog {
  protected[analyzer] def sortAfterColumnNumeric(column: Int)(a1: Array[String], a2: Array[String]): Boolean = {
    require(a1.size > column && a2.size > column, s"Both arrays need to have a $column column")
    def parse(s: String) = java.lang.Double.parseDouble(s)
    val e1 = parse(a1(column))
    val e2 = parse(a2(column))
    e1.compareTo(e2) < 0
  }
}
