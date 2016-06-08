package innoz.gui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class ContentPane extends JPanel {

	public ContentPane() {

        setOpaque(true);

    }

    @Override
    protected void paintComponent(Graphics g) {

        // Allow super to paint
        super.paintComponent(g);

        // Apply our own painting effect
        Graphics2D g2d = (Graphics2D) g.create();
        // 50% transparent Alpha
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        g2d.setColor(getBackground());
        g2d.fill(getBounds());

        g2d.dispose();

    }
	
}
