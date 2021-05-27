import java.util.ArrayList;

/**
 * Calculate light-mesh collisions to generate image from camera
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
	final static Vector3 camLoc = new Vector3(0, 0, 10);
	final static Vector3 camRot = new Vector3(0, 0, 0);

	// scene info
	private static ArrayList<TriMesh> scene = new ArrayList<TriMesh>();

	// display image
	private static Display display = new Display(WIDTH, HEIGHT);

	/**
	 * Load meshes, generate final image
	 */
	public static void main(String[] args) {
		// load meshes
		Vector3 scale = new Vector3(5, 1, 1);
		Vector3 rot = new Vector3(0, 0, 0);
		Vector3 trans = new Vector3(0, 0, 0);
		TriMesh ico = new TriMesh("icosahedron.obj", scale, rot, trans);
		TriMesh face = new TriMesh("face.obj");
		scene.add(face);
		scene.add(ico);

		// generate image
		// render();
		view();
	}

	/**
	 * Generate image from light-mesh collisions
	 */
	public static void render() {
		// generate camera rotation matrix
		double[][] camRotMat = Vector3.getRotMat(camRot);

		// calculate value for each pixel
		double step = 2 * Math.tan(FOV) / WIDTH;
		Vector3 camRay;
		Vector3 hit = new Vector3(0, 0, 0);
		Vector3 tuv = new Vector3(0, 0, 0);
		Vector3 n = new Vector3(0, 0, 0);
		Vector3 t1 = new Vector3(0, 0, 0);
		Vector3 t2 = new Vector3(0, 0, 0);
		int color = 0;

		// loop through pixels
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				// direction of ray for pixel ij
				camRay = new Vector3(step * (i - (WIDTH / 2)),
						step * ((HEIGHT / 2) - j), -1).mul(camRotMat).norm();

				// color test
				if (step * (i - (WIDTH / 2)) > 0) {
					if (step * ((HEIGHT / 2) - j) > 0) {
						// (+,+)
						display.set(i, j, 255, 255, 100);
					} else {
						// (+,-)
						display.set(i, j, 255, 100, 100);
					}
				} else {
					if (step * ((HEIGHT / 2) - j) > 0) {
						// (-,+)
						display.set(i, j, 100, 255, 100);
					} else {
						// (-,-)
						display.set(i, j, 100, 100, 255);
					}
				}

				// check intersection (any object in scene)
				if (collision(camLoc, camRay, hit, tuv, n, t1, t2)) {
					color = (int) Math.round(camRay.mul(1).dot(n) * -255);
					display.set(i, j, color, color, color);
				} else {
					display.set(i, j, 201, 226, 255);
				}
			}
			display.repaint(); // update image
		}
	}

	/**
	 * Quickly generate an image without lighting to test the scene
	 */
	public static void view() {
		// generate camera rotation matrix
		double[][] camRotMat = Vector3.getRotMat(camRot);

		// calculate value for each pixel
		double step = 2 * Math.tan(FOV) / WIDTH;
		Vector3 camRay;
		Vector3 hit = new Vector3(0, 0, 0);
		Vector3 tuv = new Vector3(0, 0, 0);
		Vector3 n = new Vector3(0, 0, 0);
		Vector3 t1 = new Vector3(0, 0, 0);
		Vector3 t2 = new Vector3(0, 0, 0);
		int color = 0;

		// loop through pixels
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				// direction of ray for pixel ij
				camRay = new Vector3(step * (i - (WIDTH / 2)),
						step * ((HEIGHT / 2) - j), -1).mul(camRotMat).norm();

				// check intersection (any object in scene)
				if (collision(camLoc, camRay, hit, tuv, n, t1, t2)) {
					color = (int) Math.round(camRay.mul(1).dot(n) * -255);
					display.set(i, j, color, color, color);
				} else {
					display.set(i, j, 201, 226, 255);
				}
			}
			display.repaint(); // update image
		}
	}

	/**
	 * Check meshes in scene for ray intersection. The closest intersection is
	 * where the light will collide and scatter.
	 * 
	 * @param o   origin of ray
	 * @param d   direction of ray
	 * @param hit collision coords
	 * @param tuv barycentric coords
	 * @param n   normal
	 * @return false if no intersection
	 */
	private static boolean collision(Vector3 o, Vector3 d, Vector3 hit,
			Vector3 tuv, Vector3 n, Vector3 t1, Vector3 t2) {

		// store intersection info of current triangle
		double dist = Double.MAX_VALUE;
		Vector3 hitC = new Vector3(0, 0, 0);
		Vector3 tuvC = new Vector3(0, 0, 0);
		Vector3 nC = new Vector3(0, 0, 0);
		Vector3 t1C = new Vector3(0, 0, 0);
		Vector3 t2C = new Vector3(0, 0, 0);

		// check all objects in scene
		for (int i = 0; i < scene.size(); i++) {
			// if there is a closer collision
			if (scene.get(i).meshInt(o, d, hitC, tuvC, nC, t2C, t2C)
					&& tuvC.getX() < dist) {
				// update intersection info
				dist = tuvC.getX();
				hit.set(hitC);
				tuv.set(tuvC);
				n.set(nC);
				t1.set(t1C);
				t2.set(t2C);
			}
		}
		return dist < Double.MAX_VALUE;
	}
}