import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Generates image from camera
 * 
 * @author Jeremy Parker Yang
 * 
 */
public class Camera {

	// camera info
	final static int WIDTH = 640;
	final static int HEIGHT = 480;
	final static double FOV = 0.69;

	// camera position
	static Vector3 camLoc = new Vector3(0, 0, 5);
	// TODO static Vector3 camRot = new Vector3();

	// display image
	private static JFrame frame;
	private static BufferedImage image;
	private static DataBuffer data;
	private static JPanel panel;

	// triangle test
	static Vector3 offset = new Vector3(0,0,0);
	static Vector3 a = new Vector3(0, 0, 0).add(offset);
	static Vector3 b = new Vector3(1, 0, 0).add(offset);
	static Vector3 c = new Vector3(0, 1, 0).add(offset);
	static Triangle tri = new Triangle(a, b, c);

	/**
	 * Load meshes, generate final image
	 */
	public static void main(String[] args) {

		// add meshes to scene
//		// mesh test
//		TriMesh icosahedron = new TriMesh("Icosahedron.obj");
//		boolean intersect = false;

		// render
		render();

	}

	public static void render() {

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
		panel.repaint();

		// calculate value for each pixel
		double step = 2 * Math.tan(FOV) / WIDTH;
		Vector3 camRay;
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {

				camRay = new Vector3(step * (i - (WIDTH / 2)), step * ((HEIGHT / 2) - j), -1);

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
				if (tri.MTint(camLoc, camRay, null)) {
					// intersects
					set(i, j, 255, 255, 255);
				}

				panel.repaint();
			}
		}

	}

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

}
