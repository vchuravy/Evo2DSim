package org.vastness.evo2dsim

import org.vastness.evo2dsim.evolution.Genome
import scalaz.{TreeLoc, Tree}
import scala.annotation.tailrec
import scalax.file.Path
import spray.json._
import org.vastness.evo2dsim.utils.MyJsonProtocol._

/**
 * @author Valentin Churavy
 */
object AnalyzeDataApp {


  def main(args : Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[String]('d', "dir") action { (x, c) =>
        c.copy(dir = x) } text "eval directory"
      opt[Int]('g', "gen") action { (x, c) =>
        c.copy(gen = x) } text "generation to lokk at"
    }

    parser.parse(args, Config()) map { config =>
      Path(config.dir) match {
        case dir: Path if dir.isDirectory => {
          dir resolve "Gen_%04d.json".format(config.gen) match {
            case file: Path if file.exists && file.isFile => {
              val genomes = file.string.asJson.convertTo[Map[String, (Int, (Double, Genome))]].map{ case (_,v) => v._1 -> v._2 }
              val tree = constructTree(genomes)
              val output = dir / "Tree"
              output.write(tree.drawTree)
            }
            case _ => println("Could not find generation")
          }
          //val childs = dir.children()
          //childs.toList.sortWith{ (p1, p2) => (p1.simpleName.trim compareToIgnoreCase p2.simpleName.trim) < 0}
          //println(childs.head.simpleName)
        }
        case _ => println("No directory")
      }
      // constructTree(result).map(_.toString.intern).drawTree

    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  case class Config(dir: String = "", gen: Int = 0)


  /**
   * Currently only constructs trees where no crossover happend.
   * @param result
   */
  def constructTree(result: Map[Int, (Double, Genome)]): Tree[Int] = {
    val genomes = result.map(_._2._2)
    val history = genomes.map(_.history.reverse).toList
    _constructTree(Tree.leaf[Int](-1),history)
  }

  def _constructTree(tree: Tree[Int], history: List[List[Int]]):Tree[Int] = {
    var t = tree.loc
    for(path <- history) {
      t = createTreeFromPath(path, t.root)
    }
    t.toTree
  }

  @tailrec
  private def createTreeFromPath(path: List[Int], t: TreeLoc[Int]): TreeLoc[Int] = path match {
    case x :: xs => {
      t.findChild(_.rootLabel == x) match {
        case Some(child) => createTreeFromPath(xs, child)
        case None => {
          val newT = t.insertDownFirst(Tree.leaf(x))
          createTreeFromPath(xs, newT)
        }
      }
    }
    case _ => t
  }
}
