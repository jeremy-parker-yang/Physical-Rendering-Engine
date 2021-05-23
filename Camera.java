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
	final static int WIDTH = 1024;
	final static int HEIGHT = 640;
	final static double FOV = 0.69;

	// camera position
	static Vector3 camLoc = new Vector3(0, 0, 7);
	// TODO static Vector3 camRot = new Vector3();

	// display image
	private static JFrame frame;
	private static BufferedImage image;
	private static DataBuffer data;
	private static JPanel panel;

	// object test
	private static TriMesh obj = new TriMesh("Icosahedron.obj");

	/**
	 * Load meshes, generate final image
	 */
	public static void main(String[] args) {

		// load meshes
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
		Vector3 hit = new Vector3(0,0,0);
		Vector3 tuv = new Vector3(0,0,0);
		Vector3 n = new Vector3(0,0,0);
		int color = 0;

		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				camRay = new Vector3(step * (i - (WIDTH / 2)), step * ((HEIGHT / 2) - j), -1);
				camRay = camRay.norm();

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
				if (obj.intersect(camLoc, camRay, hit, tuv, n)) {
					color = (int) Math.round(camRay.mul(-1).dot(n) * 255);
					//System.out.println(color);
					set(i, j, color, color, color);
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
