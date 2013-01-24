package charts

import org.jfree.chart.JFreeChart
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

trait Chart {
  def width: Int

  def height: Int

  def jfreeChart: JFreeChart

  def toPng: Array[Byte] = {
    val image = jfreeChart.createBufferedImage(width, height)
    val out = new ByteArrayOutputStream()

    ImageIO.write(image, "png", out)

    out.toByteArray
  }
}
