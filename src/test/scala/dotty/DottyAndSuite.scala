package dotty

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Path, Paths}

import net.sourceforge.plantuml.SourceStringReader
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scalameta.UMLCollector
import scalameta.util.context.GlobalContext

import scala.meta.inputs.Input
import scala.meta.{Source, dialects}

class DottyAndSuite extends AnyFreeSpec with Matchers {

  val path: Path = Paths.get("src","test","scala","assets","dotty","and","and.txt")

  "Dotty Reference to Intersectiontypes can be processed to a plantUML png" in {
    val bytes = Files.readAllBytes(path)
    val fileString  = new String(bytes,"UTF-8")
    val vFile = Input.VirtualFile(path.toString,fileString)
    val input = dialects.Dotty(vFile).parse[Source].get

    val globalScope = scalameta.util.namespaces.collector.SourcesCollector(List((input,path.toAbsolutePath.toString)))
    val umlCollector = UMLCollector(input,GlobalContext(globalScope.resultingMap.map{
      case (k,v) => (k,v.map(_._1))
    }))

    val reader = new SourceStringReader(umlCollector.plantUMLUnit.pretty)
    val filePath = new File("src/test/scala/assets/out/and/")

    filePath.mkdirs()

    val fos = new FileOutputStream(new File(filePath.getPath + "/dottyAnd.png"))
    val sec = reader.generateImage(fos)

    sec must not be null
  }
}
