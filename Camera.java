import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Generates image from camera
 * 
 * @author Jeremy Parker Yang
 * @author Joshua Hopwood
 */
public class Camera {

	// camera info
	final static int WIDTH = 1024;
	final static int HEIGHT = 640;
	final static double FOV = 0.69;

	// camera position
	static Vector3 camLoc = new Vector3(0, 0, 10);
	// TODO static Vector3 camRot = new Vector3();

	// scene info
	private static ArrayList<TriMesh> scene = new ArrayList<TriMesh>();

	/**
	 * Load meshes, generate final image
	 */
	public static void main(String[] args) {
		// load meshes
		TriMesh ico = new TriMesh("face.obj", new Vector3(2, 1, 1), new Vector3(1.57, 0, .5), new Vector3(0, 2.5, 0));
		scene.add(ico);

		// render
		render();
	}

	/**
	 * Calculate color for each pixel
	 */
	public static void render() {
		// start image display
		imageSetup();

		// calculate value for each pixel
		double step = 2 * Math.tan(FOV) / WIDTH;
		Vector3 camRay;
		Vector3 hit = new Vector3(0, 0, 0);
		Vector3 tuv = new Vector3(0, 0, 0);
		Vector3 n = new Vector3(0, 0, 0);
		int color = 0;

		// loop through pixels
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				// direction of ray for pixel ij
				camRay = new Vector3(step * (i - (WIDTH / 2)), step * ((HEIGHT / 2) - j), -1).norm();

				// color test
				if (step * (i - (WIDTH / 2)) > 0) {
					if (step * ((HEIGHT / 2) - j) > 0) {
						// (+,+)
						set(i, j, 255, 255, 100);
					} else {
						// (+,-)
						set(i, j, 255, 100, 100);
					}
				} else {
					if (step * ((HEIGHT / 2) - j) > 0) {
						// (-,+)
						set(i, j, 100, 255, 100);
					} else {
						// (-,-)
						set(i, j, 100, 100, 255);
					}
				}

				// check intersection
				for (int k = 0; k < scene.size(); k++) {
					if (scene.get(k).intersect(camLoc, camRay, hit, tuv, n)) {
						color = (int) Math.round(camRay.mul(-1).dot(n) * 255);
						// System.out.println(color);
						set(i, j, color, color, color);
					}
				}
			}
			panel.repaint(); // update image
		}

	}

	// data to display image
	private static JFrame frame;
	private static BufferedImage image;
	private static DataBuffer data;
	private static JPanel panel;

	/**
	 * Sets the color of one pixel
	 * 
	 * @param x position of pixel
	 * @param y position of pixel
	 * @param r red value [0,255]
	 * @param g green value [0,255]
	 * @param b blue value [0,255]
	 */
	private static void set(int x, int y, int r, int g, int b) {
		data.setElem(x + y * WIDTH, r << 16 | g << 8 | b);
	}

	/**
	 * Sets up image to display
	 */
	public static void imageSetup() {
		// create frame to display image
		frame = new JFrame("path tracer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(false);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// define image
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		data = image.getRaster().getDataBuffer();
		panel = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				g.drawImage(image, 0, 0, null);
			}
		};

		// add image to panel
		frame.add(panel);
		frame.validate();
		for (int y = 0; y < HEIGHT; y++)
			for (int x = 0; x < WIDTH; x++)
				data.setElem(x + y * WIDTH, 0x00000000);
	}

}
