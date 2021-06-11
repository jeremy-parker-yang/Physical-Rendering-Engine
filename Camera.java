import java.util.ArrayList;

/**
 * Calculate light-mesh collisions to generate image from camera
 * 
 * @author Jeremy Parker Yang
 */
public class Camera {

	// image quality parameters
	final static int WIDTH = 640; // number of pixels across x axis
	final static int HEIGHT = 480; // number of pixels across y axis
	final static int AA_SAMPLES = 10; // anti-aliasing samples
	final static int SAMPLES = 32; // max number of scattered rays
	final static int MAX_BOUNCES = 3; // max number of GI bounces
	final static double GI_SCALE = 0.4; // GI intensity falloff

	// camera info
	final static Vector3 CAM_LOC = new Vector3(10, 7.5, 10).mul(0.3);
	final static Vector3 CAM_ROT = new Vector3(-0.5, 0.785, 0);
	final static double FOV = 0.69;
	static ArrayList<TriMesh> scene = new ArrayList<TriMesh>();

	// display image
	private static Display display = new Display(WIDTH, HEIGHT);

	/**
	 * Load meshes, generate image
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
		// data to send rays from camera
		double[][] camRotMat = Vector3.getRotMat(CAM_ROT);
		double step = 2 * Math.tan(FOV) / WIDTH;
		Vector3 camRay;

		// primary collision info
		Vector3 hit = new Vector3(0, 0, 0);
		Vector3 tuv = new Vector3(0, 0, 0);
		Vector3 n = new Vector3(0, 0, 0);
		Vector3 t1 = new Vector3(0, 0, 0);
		Vector3 t2 = new Vector3(0, 0, 0);

		// color generating info
		int color = 0;
		double totalColor = 0;

		// loop through pixels
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {

				// loop through anti-aliasing samples
				Vector3 pixelColor = new Vector3(0, 0, 0);
				for (int ip = 0; ip < AA_SAMPLES; ip++) {
					for (int jp = 0; jp < AA_SAMPLES; jp++) {

						// direction of ray for pixel ij
						// for anti-aliasing, subsample with ip,jp
						camRay = new Vector3(
								// iterate horizontally
								step * (j - (WIDTH / 2))
										+ (step / (2 * AA_SAMPLES))
												* (2 * jp - (AA_SAMPLES - 1)),
								// iterate vertically
								step * ((HEIGHT / 2) - i)
										+ (step / (2 * AA_SAMPLES))
												* (2 * ip - (AA_SAMPLES - 1)),
								-1).mul(camRotMat).norm();

						// if camRay intersects with any mesh
						if (collision(CAM_LOC, camRay, hit, tuv, n, t1, t2)) {

							// scatter light from point of collision
							totalColor = luminance(hit, tuv, n, t1, t2,
									MAX_BOUNCES, SAMPLES);

							// paint to screen
							color = (int) (255 * totalColor);
							if (color > 255 || color < 0) {
								color = Math.max(0, color);
								color = Math.min(255, color);
							}
							pixelColor = pixelColor
									.add(new Vector3(color, color, color));
						}

						// if no collision, display sky
						else {
							pixelColor = pixelColor
									.add(new Vector3(201, 226, 255));
						}
					}
				}

				// pixel color is avg of each sub-sample
				pixelColor = pixelColor.mul(1d / (AA_SAMPLES * AA_SAMPLES));
				display.set(j, i, (int) pixelColor.getX(),
						(int) pixelColor.getY(), (int) pixelColor.getZ());
			}
			display.repaint(); // update image
		}
	}

	/**
	 * Sum of all contributions of light that collide with point hit and scatter
	 * in the direction of the camera or an intermediate surface. This is a
	 * recursive algorithm with a maximum ray depth of numBounces. After each
	 * collision with a surface, the number of scattered rays is cut in half.
	 * 
	 * @param hit        the point on a surface in cartesian coordinates
	 * @param tuv        the point in barycentric coordinates
	 * @param n          the normal of the surface at point hit
	 * @param t1         tangent 1 of the surface at point hit
	 * @param t2         tangent 2 of the surface at point hit
	 * @param numBounces the maximum number of remaining bounces
	 * @return luminance at point hit in direction of camera or surface
	 */
	public static double luminance(Vector3 hit, Vector3 tuv, Vector3 n,
			Vector3 t1, Vector3 t2, int numBounces, int samples) {

		// reached end of recursive limit
		if (numBounces < 1) {
			return 0;
		}

		// data to add up light from each sample
		Vector3 scatter = new Vector3(0, 0, 0);
		double totalLuminance = 0;

		// info for secondary light bounces
		// point s is found by scattering and colliding rays from hit
		Vector3 hitS = new Vector3(0, 0, 0);
		Vector3 tuvS = new Vector3(0, 0, 0);
		Vector3 nS = new Vector3(0, 0, 0);
		Vector3 t1S = new Vector3(0, 0, 0);
		Vector3 t2S = new Vector3(0, 0, 0);
		double luminanceS = 0; // luminance at s in direction of hit

		// scatter light mutliple times and sum contribution at hit
		for (int k = 0; k < samples; k++) {

			// scatter light in new direction
			scatter = Material.scatter(n, t1, t2);
			luminanceS = 0;

			// if hits mesh, add contribution from mesh
			if (collision(hit, scatter, hitS, tuvS, nS, t1S, t2S)) {

				// get light contribution from direction s (scattered ray)
				// contribution from scattered point follows inverse square law
				luminanceS = luminance(hitS, tuvS, nS, t1S, t2S, numBounces - 1,
						samples / 2)
						/ ((GI_SCALE * tuv.getX() + 1)
								* (GI_SCALE * tuv.getX() + 1));

				// add luminance from the secondary mesh collision
				totalLuminance = totalLuminance + luminanceS;
			}

			// ray hits light
			else {
				// contribution from light source
				// some light absorbed - include mesh color here
				totalLuminance = totalLuminance + SkyLight.getLight(scatter);
			}
		}

		// return luminance at point hit
		totalLuminance = totalLuminance * 0.8; // TODO: adjust for color
		return totalLuminance / samples;
	}

	/**
	 * Generate a quick, low quality image to test the scene
	 */
	public static void view() {
		// generate camera rotation matrix
		double[][] camRotMat = Vector3.getRotMat(CAM_ROT);

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
				camRay = new Vector3(
						step * (i - (WIDTH / 2)),
						step * ((HEIGHT / 2) - j),
						-1).mul(camRotMat).norm();

				// check intersection (any object in scene)
				if (collision(CAM_LOC, camRay, hit, tuv, n, t1, t2)) {
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