package org.vastness.evo2dsim.gui

import scala.swing._
import javax.swing.{JScrollPane, JPanel}

class GUI {
  var worldView: Surface = new Surface
  var panel: BorderPanel = new BorderPanel {
    override def contents = Seq(worldView)
  }
}