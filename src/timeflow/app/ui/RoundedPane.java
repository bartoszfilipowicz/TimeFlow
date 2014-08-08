package timeflow.app.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.jdesktop.swingx.image.GaussianBlurFilter;

/**
 * <p>A rounded pane with a drop shadow.</p>
 *
 * <p>Adapted from MadProgrammer's answer here: http://stackoverflow.com/a/13369038</p>
 */
public class RoundedPane extends JPanel
{
    /**
     * The content pane inside this panel.
     */
    private final JPanel contentPanel = new JPanel();

    /**
     * The size of the drop shadow.
     */
    private int shadowSize = 5;

    /**
     * The size of the corner rounding.
     */
    private int cornerSize = 12;

    public RoundedPane()
    {
        // This is very important, as part of the panel is going to be transparent
        setOpaque(false);
        add(contentPanel);
    }

    /**
     * Gets the content panel.
     *
     * @return the content panel.
     */
    public JPanel getContentPanel()
    {
        return contentPanel;
    }

    @Override
    public Insets getInsets()
    {
        if (getComponentOrientation() == ComponentOrientation.LEFT_TO_RIGHT)
        {
            return new Insets(0, 0, 10, 10);
        }
        else
        {
            return new Insets(0, 10, 0, 10);
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        int width = getWidth() - 1;
        int height = getHeight() - 1;

        Graphics2D g2d = (Graphics2D) g.create();
        applyQualityProperties(g2d);
        Insets insets = getInsets();
        Rectangle bounds = getBounds();
        bounds.x = insets.left;
        bounds.y = insets.top;
        bounds.width = width - (insets.left + insets.right);
        bounds.height = height - (insets.top + insets.bottom);

        RoundRectangle2D shape = new RoundRectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height, cornerSize, cornerSize);

        /**
         ** THIS SHOULD BE CACHED AND ONLY UPDATED WHEN THE SIZE OF THE COMPONENT CHANGES **
         */
        BufferedImage img = createCompatibleImage(bounds.width, bounds.height);
        Graphics2D tg2d = img.createGraphics();
        applyQualityProperties(g2d);
        tg2d.setColor(Color.BLACK);
        tg2d.translate(-bounds.x, -bounds.y);
        tg2d.fill(shape);
        tg2d.dispose();
        BufferedImage shadow = generateShadow(img, shadowSize, Color.BLACK, 0.5f);

        g2d.drawImage(shadow, shadowSize, shadowSize, this);

        g2d.setColor(getBackground());
        g2d.fill(shape);

        /**
         * THIS ONE OF THE ONLY OCCASIONS THAT I WOULDN'T CALL super.paintComponent *
         */
        getUI().paint(g2d, this);

        g2d.setColor(Color.GRAY);
        g2d.draw(shape);
        g2d.dispose();
    }

    public BufferedImage createCompatibleImage(int width, int height)
    {
        return createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    public static void applyQualityProperties(Graphics2D g2)
    {
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public BufferedImage createCompatibleImage(int width, int height, int transparency)
    {
        BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        image.coerceData(true);
        return image;
    }

    public BufferedImage generateShadow(BufferedImage imgSource, int size, Color color, float alpha)
    {
        int imgWidth = imgSource.getWidth() + (size * 2);
        int imgHeight = imgSource.getHeight() + (size * 2);

        BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight);
        Graphics2D g2 = imgMask.createGraphics();
        applyQualityProperties(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.dispose();

        return generateBlur(imgMask, size, color, alpha);
    }

    public BufferedImage generateBlur(BufferedImage imgSource, int size, Color color, float alpha)
    {
        GaussianBlurFilter filter = new GaussianBlurFilter(size);

        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgBlur = createCompatibleImage(imgWidth, imgHeight);
        Graphics2D g2d = imgBlur.createGraphics();
        applyQualityProperties(g2d);

        g2d.drawImage(imgSource, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2d.setColor(color);

        g2d.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2d.dispose();

        imgBlur = filter.filter(imgBlur, null);

        return imgBlur;
    }
}