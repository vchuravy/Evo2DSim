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

import org.vastness.evo2dsim.gui._
import spray.json._
import org.vastness.evo2dsim.utils.MyJsonProtocol._
import scala.swing._
import scalax.file.Path
import scalax.io.Input
import java.io.File
import org.vastness.evo2dsim.evolution.Genome
import org.vastness.evo2dsim.environment.BasicEnvironment
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.swing.event.{Key, KeyPressed}
import org.vastness.evo2dsim.simulator.light.{LightCategory, LightManager}


class MainWindow extends MainFrame {
  val worldView: Surface = new Surface
  var e: Option[BasicEnvironment] = None

  var timeStep: Int = 50
  var dataDir: Option[File] = None
  var evalDir: Option[Path] = None
  var group: Int = 0
  var generation: Map[Int, (Double, Genome)] = Map.empty

  def lm: Option[LightManager] = e.map(_.sim.lightManager)

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
        case FileChooser.Result.Approve => Some(fc.selectedFile)
        case _ => None
      }
    })
    contents += new MenuItem( Action("Select evaluation run") {
      evalDir = dataDir map showEvaluations
    })
    contents += new MenuItem( Action("Select generation") {
      generation = evalDir map showStats match {
        case Some(gen) => loadGeneration(evalDir.get, gen)
        case None => Map.empty
      }
    })
    contents += new MenuItem( Action("Select group") {
      group = showGroups
    })
    contents += new MenuItem( Action("Init") {
      initEnvironment()
    })
  }
  contents = new BorderPanel {
    import BorderPanel._
    add(worldView, Position.Center)

    listenTo(keys)
    reactions += {
      case KeyPressed(_, Key.R, _, _)                     => initEnvironment()
      case KeyPressed(_, Key.A, Key.Modifier.Control, _)  => lm.map(_.disableCategory(LightCategory.AgentLight))
      case KeyPressed(_, Key.A, Key.Modifier.Shift, _)    => lm.map(_.enableCategory(LightCategory.AgentLight))
      case KeyPressed(_, Key.A, _, _)                     => lm.map(_.toggleCategory(LightCategory.AgentLight))
      case KeyPressed(_, Key.F, Key.Modifier.Control, _)  => lm.map(_.disableCategory(LightCategory.FoodSourceLight))
      case KeyPressed(_, Key.F, Key.Modifier.Shift, _)    => lm.map(_.enableCategory(LightCategory.FoodSourceLight))
      case KeyPressed(_, Key.F, _, _)                     => lm.map(_.toggleCategory(LightCategory.FoodSourceLight))
    }

    focusable = true
    requestFocus()
  }

  size = new Dimension(300, 200)


  def loadGeneration(dir: Path, generation: Int): Map[Int, (Double, Genome)] = {
    val genFile = dir resolve "Gen_%04d.json".format(generation)
    val input: Input = genFile.inputStream()
    val gen = input.string.asJson.convertTo[Map[String, (Double, Genome)]]
    gen.map (x => x._1.toInt -> x._2)
  }

  def loadEvaluations(dir: Path) = {
    (IndexedSeq("Dir"), dir.children().toArray.sorted map {c => Array(c.name)} )
  }
  def showEvaluations(jDir: File): Path = {
    val (columnNames, rowData) = loadEvaluations(Path(jDir))
    val table = new Table(mapToAny(rowData) , columnNames.toSeq)
    val tD = new TableDialog(this, table)
    tD.showTableDialog()
    val index = tD.selectedValue
    val result = rowData(index).head
    Path(jDir) resolve result
  }

  def loadStats(dir: Path) = {
    val statsFile = dir resolve "Stats.csv"
    val in = statsFile.inputStream()
    val csv: Traversable[Array[String]] = in.lines().map (_ split ',')
    val columnNames = csv.head.toIndexedSeq
    val columns = csv.tail.map {
      case Array(index, max, min, mean, variance) => Array(index.toInt, max.toDouble, min.toDouble, mean.toDouble, variance.toDouble)
    }.toArray.sortBy(_(3)).reverse
    (columnNames, columns)
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
    val groups = generation.toList.sortBy(_._1).grouped(size)
    val groupPerformance = groups.map(e => e.foldLeft[Double](0.0){ case (acc, (_, (fitness, _))) => acc + fitness}).toIndexedSeq
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

    val e = new BasicEnvironment(timeStep, 0)
    e.initializeStatic()
    e.initializeAgents(generation.grouped(10).toIndexedSeq(group))
    EnvironmentManager.addEnvironment(e)
    e.sim.lightManager.disabledCategories = disabledLightSourceCategories
    future {
      e.run()
    }
    this.e = Some(e)
  }

  private def mapToAny[A <: Any](array: Array[Array[A]]) = array map { _ map { _.asInstanceOf[Any] }}
}
