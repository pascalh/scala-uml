package dotty

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Path, Paths}

import net.sourceforge.plantuml.SourceStringReader
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scalameta.UMLCollector
import scalameta.util.context.GlobalContext

import scala.meta.{Source, dialects}
import scala.meta.inputs.Input

class DottyUnionSuite extends AnyFreeSpec with Matchers {

  val path: Path = Paths.get("src","test","scala","assets","dotty","union","union.txt")

  "Dotty Reference to Intersectiontypes can be processed to a plantUML png" in {
    val bytes = Files.readAllBytes(path)
    val fileString  = new String(bytes,"UTF-8")
    val vFile = Input.VirtualFile(path.toString,fileString)
    val input = dialects.Dotty(vFile).parse[Source].get

    val globalScope = scalameta.util.namespaces.collector.SourcesCollector(List((input,path.toAbsolutePath.toString)))
    val umlCollector = UMLCollector(input,GlobalContext(globalScope.resultingMap),path.toAbsolutePath.toString)

    val reader = new SourceStringReader(umlCollector.plantUMLUnit.pretty)
    val filePath = new File("src/test/scala/assets/out/union/")

    filePath.mkdirs()

    val fos = new FileOutputStream(new File(filePath.getPath + "/dottyUnion.png"))
    val sec = reader.generateImage(fos)

    sec must not be null
  }
}
