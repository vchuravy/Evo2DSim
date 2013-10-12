package org.vastness.evo2dsim

import org.vastness.evo2dsim.simulator.World
import org.vastness.evo2dsim.gui.GUI
import javax.swing.JFrame

/**
 * @author Valentin Churavy
 */
object App {
  val world = new World
  def getWorld = world
  
  
  def main(args : Array[String]) {
    println( "Evo2DSim is a simple simulator for evolutionary experiments." )
    //timeStep per Second
    val timeStep = 1.0f / 60.0f // 60fps
    println("Running simulation for one simulation minute")

    val frame: JFrame = new JFrame("GUI")
    frame.setContentPane(new GUI().getPanel())
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.pack
    frame.setVisible(true)
    for(i: Int <- 1 to 3600) {
      world.step(timeStep)
    }
  }

}
