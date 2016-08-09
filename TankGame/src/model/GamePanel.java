package model;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements MouseListener {

	private static final long serialVersionUID = 8337292632240105367L;
	private static final GamePanel gamePanel = new GamePanel();
	private Canvas paintCanvas = new Canvas();

	public static GamePanel getGamePanel() {
		return gamePanel;
	}

	private BufferedImage renderImage = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration()
			.createCompatibleImage(Constants.WIDTH, Constants.HEIGHT);
	private Graphics2D renderGraphics = renderImage.createGraphics();

	private GamePanel() {
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		layout.setHgap(0);
		layout.setVgap(0);
		paintCanvas.setSize(Constants.WIDTH, Constants.HEIGHT);
		setIgnoreRepaint(true);
		paintCanvas.setIgnoreRepaint(true);
		paintCanvas.addMouseListener(this);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(paintCanvas, BorderLayout.CENTER);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Constants.WIDTH, Constants.HEIGHT);
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		ControlPanel.getControlPanel().clickedMouse(event.getX(), event.getY());
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	public void render() {

		renderGraphics.setBackground(Color.WHITE);
		renderGraphics.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

		Renderable[] renderables = World.getWorld().getAllRenderables();
		Arrays.sort(renderables, RenderSorter.getRenderSorter());
		for (Renderable renderable : renderables) {
			renderable.render(renderGraphics);
			RenderHelper.setAntialias(renderGraphics, true);
		}

		Graphics graphics = paintCanvas.getGraphics();
		graphics.drawImage(renderImage, 0, 0, null);
		graphics.dispose();

	}

}
