package app.frontend.processor

import app.frontend.exceptions.{BadInputPathException, BadOutputPathException, GithubConfigFailedException, UMLConversionException}
import app.github.{GithubLoader, PublicGithub}
import net.sourceforge.plantuml.{FileFormat, FileFormatOption, SourceStringReader}
import org.slf4j.LoggerFactory
import pretty.config.PlantUMLConfig
import pretty.plantuml.UMLUnitPretty
import pureconfig.ConfigSource
import scalameta.toplevel.SourcesCollector
import uml.UMLUnit
import uml.umlMethods.toPackageRep
import pureconfig._
import pureconfig.generic.auto._

import java.io.{File, FileNotFoundException, FileOutputStream, IOException}

case class GithubUMLDiagramProcessor(
                                      outputPath:String,
                                      githubConfigPath:String,
                                      isVerbose:Boolean,
                                      name:String="default")
  extends Processor {

  override def execute(): Unit = {
    val logger = LoggerFactory.getLogger("execution")

    val githubRepoLoaded = ConfigSource.file(githubConfigPath).load[PublicGithub]

    val  githubRepo = githubRepoLoaded match {
      case Left(value) =>
        logger.debug(s"Github config at: [$githubConfigPath] with message: ${value.toString()}")
        throw new BadInputPathException(s"Github config at: [$githubConfigPath] is corrupt.")
      case Right(dirs) => dirs
    }
    val loadedGithub = try {
      GithubLoader(githubRepo)
    } catch {
      case exception: Exception =>
        throw new GithubConfigFailedException(s"Config found at: [$githubConfigPath] is corrupt.",exception)
    }

    logFoundScalaFiles(loadedGithub)

    val umlProcess = try {
      SourcesCollector(
        loadedGithub
          .repo
          .indexedFiles
          .map {
            case (s, sources) => (s, sources.headOption.getOrElse(throw new IllegalStateException()))
          }
          .toList
          .map(tp => tp.swap),
        name)
    }  catch {
      case ni:NotImplementedError =>
        throw new UMLConversionException(s"Files contain features that are not yet supported: ${ni.getMessage}",ni)
      case e:Exception =>
        throw new UMLConversionException(s"Unknown error when processing. try --verbose to get debug information.")
    }

    val path = if(outputPath.isEmpty){
      logger.info(s"No output path specified. Assuming:" +
        s" ${ClassLoader.getSystemClassLoader.getResource(".").getPath} as output path." +
        s" Try --d <path> to define output path.")
        ClassLoader.getSystemClassLoader.getResource(".").getPath
      } else {
        outputPath
      }

      implicit val prettyPrinter: UMLUnitPretty = UMLUnitPretty()(PlantUMLConfig())

      val packageRep = try {
        toPackageRep(umlProcess.umlUnit).value.asInstanceOf[UMLUnit]
      } catch {
        case e:Exception => throw e
      }
      println(packageRep.pretty)
      val reader = new SourceStringReader(packageRep.pretty)
      val filePath = new File(path)

    val fos = try {
      println(filePath.isFile)
      if(filePath.isDirectory) {
        new FileOutputStream(new File(filePath.getPath + "/" + name + ".svg"))
      } else {
        println("Here")
        new FileOutputStream(new File(filePath.getPath + name + ".svg"))
      }
    } catch {
      case fnf:FileNotFoundException => throw new BadOutputPathException(
        s"specified output path: [${filePath.getPath}] is invalid. Try --d <path> with a valid path.",
        fnf
      )
    }

    try {
      reader.generateImage(fos, new FileFormatOption(FileFormat.SVG))
      logger.info(s"Successfully exported image to location: ${filePath.getPath + name + ".svg"}")
    } catch {
      case i:IOException =>
        logger.error(s"Unable to export image: ${filePath.getPath + name + ".svg"}." +
          s" Try --verbose to get debug information.")
        logger.debug(s"${i.getStackTrace.mkString("Array(", ", ", ")")}")
    }
  }

  private def logFoundScalaFiles(loadedGithub: GithubLoader): Unit = {
    val logger = LoggerFactory.getLogger("execution")
    loadedGithub.repo.indexedFiles.foreach {
      case (s, sources) =>
        val fileName = s
        sources.collectFirst(_ => true).getOrElse(throw new IllegalStateException(""))
        logger.info(s"Found scala file file: $fileName")
    }
  }
}
