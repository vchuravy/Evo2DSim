package org.vastness.evo2dsim.gui

import javax.swing.JPanel
import java.awt.{Color, Graphics, Graphics2D}
import org.vastness.evo2dsim.App
import org.vastness.evo2dsim.simulator.Entity

class Surface extends JPanel {
  def draw(g2: Graphics2D) ={
    g2.setColor(new Color(0,0,0)) //Draw everything in black
    for(e: Entity <- App.getWorld.getEntities){
      e.sprite.draw(g2)
    }
  }

  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    g match {
      case g2: Graphics2D => draw(g2)
      case _ => throw new ClassCastException
    }
  }
}
