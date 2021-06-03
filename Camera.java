import java.util.ArrayList;

/**
 * Calculate light-mesh collisions to generate image from camera
 * 
 * @author Jeremy Parker Yang
 */
public class Camera {

	// image quality
	final static int WIDTH = 640;
	final static int HEIGHT = 480;
	final static int samples = 50;
	final static int numBounces = 3;

	// camera info
	final static Vector3 camLoc = new Vector3(10, 7.5, 10).mul(0.3);
	final static Vector3 camRot = new Vector3(-0.5, 0.785, 0);
	final static double FOV = 0.69;
	static ArrayList<TriMesh> scene = new ArrayList<TriMesh>();

	// display image
	private static Display display = new Display(WIDTH, HEIGHT);

	/**
	 * Load meshes, generate final image
	 */
	public static void main(String[] args) {
		// load meshes
		Vector3 scale = new Vector3(1, 1, 1);
		Vector3 rot = new Vector3(0, 0, 0);
		Vector3 trans = new Vector3(0, 0.5, 0);
		TriMesh cube = new TriMesh("cube.obj", scale, rot, trans);
		TriMesh plane = new TriMesh("plane.obj");
		scene.add(cube);
		scene.add(plane);

		// generate image
		render();
		// view();
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

		// primary hit info
		Vector3 hit = new Vector3(0, 0, 0);
		Vector3 tuv = new Vector3(0, 0, 0);
		Vector3 n = new Vector3(0, 0, 0);
		Vector3 t1 = new Vector3(0, 0, 0);
		Vector3 t2 = new Vector3(0, 0, 0);

		// color generating info
		int color = 0;
		double totalColor = 0;

		// loop through pixels
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {

				// direction of ray for pixel ij
				camRay = new Vector3(step * (i - (WIDTH / 2)),
						step * ((HEIGHT / 2) - j), -1).mul(camRotMat).norm();

				// if camRay intersects with a mesh
				if (collision(camLoc, camRay, hit, tuv, n, t1, t2)) {

					// scatter light from point of collision
					totalColor = totalLight(hit, tuv, n, t1, t2, numBounces);

					// paint to screen
					color = (int) (255 * totalColor);
					if (color > 255 || color < 0) {
						color = Math.max(0, color);
						color = Math.min(255, color);
					}
					display.set(i, j, color, color, color);
				}

				// if no collision, display sky
				else {
					display.set(i, j, 201, 226, 255);
				}
			}
			display.repaint(); // update image
		}
	}

	/**
	 * Sum all contributions of light that illuminate this point (called hit).
	 * This is a recursive algorithm that includes light contribution from
	 * surfaces and lights. Contributions from surfaces decrease with distance.
	 * 
	 * @param hit        the point on a mesh to sum light at
	 * @param tuv        the point hit in barycentric coordinates
	 * @param n          the normal of the surface at point hit
	 * @param t1         a tangent of the surface at point hit
	 * @param t2         a tangent of the surface at point hit
	 * @param numBounces the maximum number of remaining bounces
	 * @return the contribution of light from all sources at point hit
	 */
	public static double totalLight(Vector3 hit, Vector3 tuv, Vector3 n,
			Vector3 t1, Vector3 t2, int numBounces) {

		// reached end of recursive limit
		if (numBounces < 1) {
			return 0;
		}

		// add up light from each sample
		Vector3 scatter = new Vector3(0, 0, 0);
		double totalColor = 0;

		// info for secondary light bounces
		// s is a point found by scattering and colliding light from hit
		Vector3 hitS = new Vector3(0, 0, 0);
		Vector3 tuvS = new Vector3(0, 0, 0);
		Vector3 nS = new Vector3(0, 0, 0);
		Vector3 t1S = new Vector3(0, 0, 0);
		Vector3 t2S = new Vector3(0, 0, 0);
		double contribitionS = 0; // contribution at s
		double distSquared = 0; // distance from hit to s

		// scatter light mutliple times and sum contribution at hit
		for (int k = 0; k < samples; k++) {

			// scatter light in new direction
			scatter = Material.scatter(n, t1, t2);
			contribitionS = 0;

			// if hits mesh, add contribution from mesh
			if (collision(hit, scatter, hitS, tuvS, nS, t1S, t2S)) {

				// contribution from scattered point follows inverse square law
				// if statement ensures 1/x^2 only decreases light contribution
				distSquared = tuv.getX() * tuv.getX();
				if (distSquared < 50) {
					contribitionS = totalLight(hitS, tuvS, nS, t1S, t2S,
							numBounces - 1);
				} else {
					contribitionS = 50 * totalLight(hitS, tuvS, nS, t1S, t2S,
							numBounces - 1) / (tuv.getX() * tuv.getX());
				}

				// additionally, consider the cross section change
				// this is proportional normal * light direction
				// scatterContribution = scatterContribution * n.dot(scatter);

				// add scatterContribution to total light at the point
				totalColor = totalColor + contribitionS;
			}

			// ray hits light
			else {
				// contribution from light source
				// some light absorbed - include mesh color here
				totalColor = totalColor + SkyLight.getLight(scatter);
			}
		}
		
		// return total contribution of light at point hit
		return totalColor / samples;
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