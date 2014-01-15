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
import org.vastness.evo2dsim.gui.RenderManager

class ComponentDialog(owner: Window, component: Component, manager: RenderManager) extends Dialog(owner) {
  setLocationRelativeTo(owner)

  val ok = new Button(Action("Ok") {
    this.visible = false
    manager.renderComponents -= component
  })

  contents = new BorderPanel {
    import BorderPanel._
    add(component, Position.Center)
    add(ok, Position.South)
  }

  def showDialog() = {
    this.pack()
    manager.renderComponents += component
    this.visible = true
  }
}
