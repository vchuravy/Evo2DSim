package org.vastness.evo2dsim

import org.vastness.evo2dsim.simulator.Simulator
import org.vastness.evo2dsim.gui.GUI
import javax.swing.{SwingUtilities, JFrame}
import java.util.Timer
import org.jbox2d.common.Vec2

/**
 * @author Valentin Churavy
 */
object App {
  val sim = new Simulator
  def getWorld = sim

  var timer = new Timer()
  var running = true

  val HERTZ = 30
  val SIM_HERTZ = 120
  val timeStep = 1.0f / (SIM_HERTZ/2) // 120fps simulation speed twice as fast timestep = 1/60 -> two times reality

  val gui = new GUI

  val random = new scala.util.Random()

  private def updateSimulation() {
    sim.step(timeStep)
  }

  private def render() {
    gui.getWorldView.repaint()
  }

  def loop() {
    timer = new Timer()
    timer.schedule(new RenderLoop, 0, 1000 / HERTZ)//new timer at 30 fps, the timing mechanism
    timer.schedule(new SimulationLoop, 0, 1000/SIM_HERTZ) // new timer at 120 fps
  }

  private class RenderLoop extends java.util.TimerTask
  {
    override def run()
    {
      render()

      if (!running)
      {
        timer.cancel()
      }
    }
  }

  private class SimulationLoop extends java.util.TimerTask
  {
    override def run()
    {
      updateSimulation()

      if (!running)
      {
        timer.cancel()
      }
    }
  }


  def main(args : Array[String]) {
    println( "Evo2DSim is a simple simulator for evolutionary experiments." )

    SwingUtilities.invokeLater(new Runnable() {
      override def run() {
        val frame: JFrame = new JFrame("GUI")
        frame.setContentPane(gui.getPanel)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.pack()
        frame.setVisible(true)
      }
    })

    // Construct a basic level
    val origin = new Vec2(0.515f,0.515f)
    val halfSize = 0.5f
    val sizes = Array[Vec2](new Vec2(-halfSize,-halfSize), new Vec2(-halfSize,halfSize), new Vec2(halfSize,halfSize), new Vec2(halfSize,-halfSize))
    val edges = for(i <- 0 until sizes.length) yield origin add sizes(i)
    sim.createWorldBoundary(edges.toArray)
    for( i <- 0 until 100){
      sim.addAgent(origin.add(new Vec2(random.nextFloat(),random.nextFloat())), sim.Agents.SBotControllerLinear)
    }
    loop()
  }

}
