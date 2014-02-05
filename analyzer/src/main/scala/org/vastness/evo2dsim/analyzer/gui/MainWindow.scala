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

package org.vastness.evo2dsim.analyzer.gui

import org.vastness.evo2dsim.core.gui._

import scala.swing._
import scalax.file.Path
import java.io.File
import org.vastness.evo2dsim.core.environment.{Environment, EnvironmentBuilder}
import scala.swing.event.{Key, KeyPressed}
import org.vastness.evo2dsim.core.simulator.light.{LightCategory, LightManager}
import org.vastness.evo2dsim.core.agents.sbot.SBotController
import org.vastness.evo2dsim.analyzer.App
import org.vastness.evo2dsim.core.evolution.Evolution.Generation
import org.vastness.evo2dsim.core.utils.InputHandler
import org.vastness.evo2dsim.core.simulator.AgentID


class MainWindow extends MainFrame with RenderManager {
  val worldView: Surface = new Surface
  var e: Option[Environment] = None
  var renderComponents = Set.empty[Component]

  var timeStep: Int = 50
  var dataDir: Option[File] = None
  var evalDir: Option[Path] = None
  var group: Int = 0
  var generation: Option[Generation] = None

  def lm: Option[LightManager] = e.map(_.sim.lightManager)

  var envBuilder: EnvironmentBuilder = EnvironmentBuilder.Basic

  title = "Evo2DSim Analyzer"

  override def closeOperation() {
    e.map(_.running = false)
    super.closeOperation()
  }

  menuBar = new MenuBar {
    contents += new MenuItem(Action ("Select data directory") {
      val fc = new FileChooser(dataDir.getOrElse(new File(".")))
      fc.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

      dataDir = fc.showOpenDialog(this) match {
        case FileChooser.Result.Approve if fc.selectedFile.exists() => Some(fc.selectedFile)
        case _ => dataDir
      }
    })
    contents += new MenuItem( Action("Select evaluation run") {
      evalDir = dataDir map {d => Path(d)} map showEvaluations
    })
    contents += new MenuItem( Action("Select generation") {
      generation = evalDir map showStats flatMap {
        gen => loadGeneration(evalDir.get, gen)
      }
    })
    contents += new MenuItem( Action("Select group") {
      group = showGroups
    })
    contents += new MenuItem( Action("Init") {
      initEnvironment()
    })
    contents += new MenuItem( Action("Env") {
      selectEnvironment()
    })
    contents += new MenuItem( Action("Agent View") {
      showAgentView()
    })
    contents += new MenuItem( Action("Export Agent") {
      exportAgent()
    })
  }
  contents = new BorderPanel {
    import BorderPanel._
    add(worldView, Position.Center)

    listenTo(keys)
    reactions += {
      case KeyPressed(_, Key.R, _, _)                     => initEnvironment()
      case KeyPressed(_, Key.T, _, _)                     => toggleText()
      case KeyPressed(_, Key.P, _, _)                     => App.togglePause()
      case KeyPressed(_, Key.A, Key.Modifier.Control, _)  => lm.map(_.disableCategory(LightCategory.AgentLight))
      case KeyPressed(_, Key.A, Key.Modifier.Shift, _)    => lm.map(_.enableCategory(LightCategory.AgentLight))
      case KeyPressed(_, Key.A, _, _)                     => lm.map(_.toggleCategory(LightCategory.AgentLight))
      case KeyPressed(_, Key.F, Key.Modifier.Control, _)  => lm.map(_.disableCategory(LightCategory.FoodSourceLight))
      case KeyPressed(_, Key.F, Key.Modifier.Shift, _)    => lm.map(_.enableCategory(LightCategory.FoodSourceLight))
      case KeyPressed(_, Key.F, _, _)                     => lm.map(_.toggleCategory(LightCategory.FoodSourceLight))
      case KeyPressed(_, Key.Comma, _, _)                 => App.changeSpeedDown()
      case KeyPressed(_, Key.Colon, _, _)                 => App.changeSpeedUp()
    }

    focusable = true
    requestFocus()
  }

  size = new Dimension(300, 200)


  def selectEnvironment() {
    envBuilder = Dialog.showInput[EnvironmentBuilder](message = "Please select an Environment", entries = EnvironmentBuilder.values.toSeq, initial = envBuilder) match {
      case Some(eBuilder) => eBuilder
      case None => envBuilder
    }
  }

  def showAgentView() {
    val agent = Dialog.showInput[Int](message = "Please choose Agent",
      entries = e map { _.agents.map(_._1.id).toSeq } getOrElse Seq.empty[Int] , initial = 0) match {
      case Some(a) => e flatMap { _.agentBySimpleID(a)}
      case None => None
    }
    agent map (_.controller match { case sC: SBotController => sC.lightSensor }) map { //TODO: Simplify
      cam => {
        val cv = new CameraView(cam)
        val dialog = new ComponentDialog(this, cv, this)
        dialog.showDialog()
      }

    }
  }

  def exportAgent() {
    val agent = Dialog.showInput[Int](message = "Please choose Agent",
      entries = e map { _.agents.map(_._1.id).toSeq } getOrElse Seq.empty[Int] , initial = 0) match {
      case Some(a) => e flatMap { _.agentBySimpleID(a)}
      case None => None
    }
    val dotFile = agent flatMap (_.controller.nn map(_.toDot))
    val id = agent map (_.id)
    (dotFile, id) match {
      case (Some(content), Some(i)) => write(i, content, "dot")
      case _ =>
    }
  }

  private def write(id: AgentID, content: String, fileEnding: String): Unit = evalDir map { dir =>
    val file = dir / s"${id.toString('-')}.$fileEnding"
    file.write(content)
  }

  def toggleText() {
    EnvironmentManager.showData = if (EnvironmentManager.showData) false else true
  }

  def loadGeneration(dir: Path, generation: Int): Option[Generation] = {
    val in = new InputHandler(dir)
    in.readGeneration(generation)
  }

  def loadEvaluations(dir: Path) = {
    (IndexedSeq("Dir"), dir.children().filter(_.isDirectory).toArray.sorted map {c => Array(c.name)} )
  }
  def showEvaluations(dir: Path): Path = {
    val (columnNames, rowData) = loadEvaluations(dir)
    val s = new PlotStats(dir)
    val table = new Table(mapToAny(rowData), columnNames.toSeq)

    val tD = new EvaluationTableDialog(this, table, s)
    tD.showTableDialog()
    val index = tD.selectedValue
    val result = rowData(index).head
    dir resolve result
  }

  def loadStats(dir: Path) = {
    val statsFile = dir resolve "Stats.csv"
    val in = statsFile.inputStream()
    val csv: Traversable[Array[String]] = in.lines().map (_ split ',')
    val columnNames = csv.head.toIndexedSeq
    val columns = csv.tail.toArray.sortWith(sortAfterColumnNumeric(3)).reverse
    (columnNames, columns)
  }

  protected def sortAfterColumnNumeric(column: Int)(a1: Array[String], a2: Array[String]): Boolean = {
    require(a1.size > column && a2.size > column, s"Both arrays need to have a $column column")
    def parse(s: String) = java.lang.Double.parseDouble(s)
    val e1 = parse(a1(column))
    val e2 = parse(a2(column))
    e1.compareTo(e2) < 0
  }

  def showStats(dir: Path): Int = {
    val (columnNames, rowData) = loadStats(dir)
    val table = new Table(mapToAny(rowData) , columnNames.toSeq)
    val tD = new TableDialog(this, table)
    tD.showTableDialog()
    val index = tD.selectedValue
    rowData(index).head.toInt
  }

  def loadGen(size: Int) = {
    val groups = generation.getOrElse(Map.empty).toList.groupBy(_._1.group)
    val groupPerformance = groups.map(e => e._2.foldLeft[Double](0.0){ case (acc, (_, (fitness, _))) => acc + fitness}).toIndexedSeq
    val rowData = ( for(i <- groupPerformance.indices) yield (i, groupPerformance(i) / size) ).toArray.sortBy(_._2).reverse map
      {case (i, gP) => Array(i,gP)}
    (IndexedSeq("Index", "GroupPerformance"), rowData)
  }

  def showGroups: Int = {
    val (columnNames, rowData) = loadGen(10)
    val table = new Table(mapToAny(rowData) , columnNames.toSeq)
    val tD = new TableDialog(this, table)
    tD.showTableDialog()
    val index = tD.selectedValue
    rowData(index).head.toInt
  }

  def initEnvironment() {
    val disabledLightSourceCategories = this.e match {
      case Some(env) =>
        EnvironmentManager.clean()
        this.e = None
        env.running = false
        env.sim.lightManager.disabledCategories
      case None => Set.empty[LightCategory]
    }

    val e = envBuilder(timeStep, 0)
    e.initializeStatic()
    val g = generation.getOrElse(Map.empty).grouped(10).toIndexedSeq(group).toMap
    e.initializeAgents(g)
    EnvironmentManager.addEnvironment(e)
    e.sim.lightManager.disabledCategories = disabledLightSourceCategories
    this.e = Some(e)
    // future {
    //   e.run()
    // }
  }

  private def mapToAny[A <: Any](array: Array[Array[A]]) = array map { _ map { _.asInstanceOf[Any] }}
}
