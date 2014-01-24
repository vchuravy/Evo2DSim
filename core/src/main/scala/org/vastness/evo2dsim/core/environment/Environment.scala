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

package org.vastness.evo2dsim.core.environment

import org.vastness.evo2dsim.core.simulator.{AgentID, Agent, Simulator}
import scala.concurrent.promise
import org.vastness.evo2dsim.core.gui.EnvironmentManager
import scala.collection.Map
import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.core.evolution.genomes.Genome
import org.vastness.evo2dsim.core.data.{Recordable, Recorder, RecordLevel}
import scala.collection.parallel.ParSeq
import scalax.file.Path
import org.vastness.evo2dsim.core.evolution.Evolution.Generation
import spire.math._
import spire.implicits._

/**
 * Implements the very basics for an environment
 */
abstract class Environment(val timeStep: Int, val steps: Int) {
  def origin: Vec2
  def halfSize: Float
  def spawnSize: Float

  def newRandomPosition: Vec2 = {
    origin add new Vec2(randomFloat * spawnSize, randomFloat * spawnSize)
  }
  protected def randomFloat: Float = (sim.random.nextFloat * 2) - 1

  // Angle in Radian
  def newRandomAngle: Float = sim.random.nextFloat * 2 * pi[Float]

  private var _stepCounter = 0
  def stepCounter = _stepCounter
  
  val sim = new Simulator(scala.util.Random.nextLong())
  var agents = Map.empty[AgentID, Agent]
  val p = promise[Environment]()

  var running = true

  var recording: Boolean = false
  var recorders: ParSeq[Recorder] = ParSeq.empty

  def updateSimulation() {
    sim.step(timeStep/1000.0f)
    _stepCounter += 1
    if(recording) recorders map {_.step()}
    if(steps == stepCounter) {
      running = false
      EnvironmentManager.remove(this)
      p success this
    }
  }

  def run(){
    while(running){
        updateSimulation()
    }
    p failure(throw new Exception)
  }

  def initializeStatic()
  def initializeAgents(genomes: Generation)

  def startRecording(rL: RecordLevel, iteration: Int, baseDir: Path) {
    recording = true
    var tempRecorders = Seq.empty[Recorder]
    if(rL.record(RecordLevel.Agents)) {
      tempRecorders ++= ( for((id, a) <- agents) yield {
        createRecorder(baseDir, iteration, "agent", id, a)
      } ).toSeq
    }

    if(rL.record(RecordLevel.Controller)) {
      tempRecorders ++= ( for((id, a) <- agents) yield {
        createRecorder(baseDir, iteration, "controller", id, a.controller)
      } ).toSeq
    }
    recorders = tempRecorders.par
  }

  def agentBySimpleID(id: Int) = {
    agents.find(_._1.id == id).map(_._2)
  }

  private def createRecorder(baseDir: Path, iteration: Int, name: String, id: AgentID, r: Recordable) = {
    val dir: Path = baseDir / (s"${id.generation}/${id.group}/$iteration", '/')
    if(dir.nonExistent) dir.createDirectory(createParents = true)
    Recorder(dir, s"${id.id}_$name", r)
  }

}
