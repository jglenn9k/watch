import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Center-crops and resizes a raster image to exact store-asset dimensions. */
public final class ResizeImage {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("usage: ResizeImage INPUT OUTPUT WIDTH HEIGHT");
        }

        BufferedImage source = ImageIO.read(new File(args[0]));
        int width = Integer.parseInt(args[2]);
        int height = Integer.parseInt(args[3]);
        double targetRatio = (double) width / height;

        int cropWidth = source.getWidth();
        int cropHeight = source.getHeight();
        if ((double) cropWidth / cropHeight > targetRatio) {
            cropWidth = (int) Math.round(cropHeight * targetRatio);
        } else {
            cropHeight = (int) Math.round(cropWidth / targetRatio);
        }

        int x = (source.getWidth() - cropWidth) / 2;
        int y = (source.getHeight() - cropHeight) / 2;
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, x, y, x + cropWidth, y + cropHeight, null);
        graphics.dispose();
        ImageIO.write(output, "png", new File(args[1]));
    }
}
