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

class TableDialog(owner: Window, table: Table) extends Dialog(owner) {
  private var result = Dialog.Result.Closed

  setLocationRelativeTo(owner)

  val ok = new Button(Action("Ok") {
    result = Dialog.Result.Ok
    peer.setVisible(false)
  })
  val cancel = new Button(Action("Cancel") {
    result = Dialog.Result.Cancel
    peer.setVisible(false)
  })

  val tablePane = new ScrollPane() {
    contents = table
  }


  contents = new BorderPanel {
    import BorderPanel._

    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += ok
      contents += cancel
    }

    add(tablePane, Position.North)
    add(buttonPanel, Position.South)
  }

  def showTableDialog() = {
    this.pack()
    this.modal = true
    this.visible = true
  }

  def selectedValue: Int = {
    val selected = table.selection.rows
    if (selected.tail.nonEmpty) println("Warning: Discarded values from selection")
    selected.headOption.getOrElse(0)
  }
}
