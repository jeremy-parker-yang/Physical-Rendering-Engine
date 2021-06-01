import java.util.ArrayList;

/**
 * Calculate light-mesh collisions to generate image from camera
 * 
 * @author Jeremy Parker Yang
 * @author Joshua Hopwood
 */
public class Camera {

	// camera info
	final static int WIDTH = 640;
	final static int HEIGHT = 480;
	final static double FOV = 0.69;

	// camera position
	final static Vector3 camLoc = new Vector3(10, 7.5, 10).mul(0.3);
	final static Vector3 camRot = new Vector3(-0.5, 0.785, 0);

	// scene info
	private static ArrayList<TriMesh> scene = new ArrayList<TriMesh>();

	// display image
	private static Display display = new Display(WIDTH, HEIGHT);

	/**
	 * Load meshes, generate final image
	 */
	public static void main(String[] args) {
		// load meshes
		Vector3 scale = new Vector3(1, 1, 1);
		Vector3 rot = new Vector3(0, 0, 0);
		Vector3 trans = new Vector3(0, .5, 0);
		TriMesh cube = new TriMesh("cube.obj", scale, rot, trans);
		TriMesh plane = new TriMesh("plane.obj");
		scene.add(cube);
		scene.add(plane);

		// generate image
		render();
		// view();
	}

	/**
	 * For testing only
	 */
	public static void render2() {
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
		Vector3 sunDir = new Vector3(-0.2, .2, .7).norm();
		int color = 0;
		double totalColor = 0;

		Vector3 abc = new Vector3(0, 0, 0);

		// loop through pixels
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				// direction of ray for pixel ij
				camRay = new Vector3(step * (i - (WIDTH / 2)),
						step * ((HEIGHT / 2) - j), -1).mul(camRotMat).norm();

				// check intersection (any object in scene)
				if (collision(camLoc, camRay, hit, tuv, n, t1, t2)) {
					// is the collision in direct sunlight?
					if (collision(hit, sunDir, abc, abc, abc, abc, abc)) {
						// not in direct sunlight
						System.out.println("not in direct sunlight");
						display.set(i, j, 0, 0, 0);
					} else {
						color = (int) (255 * n.dot(sunDir));
						if (color > 255 || color < 0) {
							System.out.println(color);
							color = Math.max(0, color);
						}
						display.set(i, j, color, color, color);
					}
				} else {
					// no collision, display sky
					display.set(i, j, 201, 226, 255);
				}
			}
			display.repaint(); // update image
		}
	}

	/**
	 * Generate high quality image from light-mesh collisions
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
		Vector3 scatter = new Vector3(0, 0, 0);
		int color = 0;
		double totalColor = 0;
		int samples = 500;

		// loop through pixels
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				// direction of ray for pixel ij
				camRay = new Vector3(step * (i - (WIDTH / 2)),
						step * ((HEIGHT / 2) - j), -1).mul(camRotMat).norm();

				// check intersection (any object in scene)
				// does camera ray hit an object?
				if (collision(camLoc, camRay, hit, tuv, n, t1, t2)) {

					// collision, scatter light
					totalColor = 0;
					for (int k = 0; k < samples; k++) {

						scatter = Material.scatter(n, t1, t2);
						Vector3 abc = new Vector3(0, 0, 0);
						// if scatter light in rand direction from hit point
						if (collision(hit, scatter, abc, abc, abc, abc, abc)) {
							// doesnt hit light - no contribution
						} else {
							totalColor = totalColor
									+ SkyLight.getLight(scatter);
						}
					}

					color = (int) (255 * (totalColor / samples));
					display.set(i, j, color, color, color);
				} else {
					// no collision, display sky
					display.set(i, j, 201, 226, 255);
				}
			}
			display.repaint(); // update image
		}
	}

	/**
	 * Quickly generate an image to test the scene
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
					color = (int) Math.round(camRay.dot(n) * -255);
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
			if (scene.get(i).meshInt(o, d, hitC, tuvC, nC, t1C, t2C)
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