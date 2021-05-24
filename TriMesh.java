import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 3D Object defined by a set of triangles
 * 
 * @author Jeremy Parker Yang
 *
 */
public class TriMesh {

	// list of all triangles
	public ArrayList<Triangle> tris = new ArrayList<Triangle>();

	// limits of bounding box
	private double xMin = Double.MAX_VALUE;
	private double yMin = Double.MAX_VALUE;
	private double zMin = Double.MAX_VALUE;
	private double xMax = -Double.MAX_VALUE;
	private double yMax = -Double.MAX_VALUE;
	private double zMax = -Double.MAX_VALUE;

	/**
	 * Default constructor. Construct mesh exactly from .obj file. Object must be
	 * triangulated and in a right-hand coordinate system.
	 * 
	 * @param fileName obj file to read
	 */
	public TriMesh(String fileName) {

		// data to generate tris and bouding box
		ArrayList<Vector3> verts = new ArrayList<Vector3>();
		double x = 0, y = 0, z = 0; // store a single vertex

		// read data from file
		try {

			// set up file scanner
			Scanner scnr = new Scanner(new File(fileName));
			String[] line;
			// x y z parts of data
			String[] pX = new String[1];
			String[] pY = new String[1];
			String[] pZ = new String[1];

			// for each line
			while (scnr.hasNextLine()) {

				// read entire line
				line = scnr.nextLine().split(" ");

				// add vertex
				if (line[0].equals("v")) {
					x = Double.valueOf(line[1]);
					y = Double.valueOf(line[2]);
					z = Double.valueOf(line[3]);
					verts.add(new Vector3(x, y, z));

					// adjust bounding box
					if (x > xMax) {
						xMax = x;
					} else if (x < xMin) {
						xMin = x;
					}
					// y component
					if (y > yMax) {
						yMax = y;
					} else if (y < yMin) {
						yMin = y;
					}
					// z component
					if (z > zMax) {
						zMax = z;
					} else if (z < zMin) {
						zMin = z;
					}
				}

				// add triangle
				else if (line[0].equals("f")) {

					// get vertex info only
					pX = line[1].split("/");
					pY = line[2].split("/");
					pZ = line[3].split("/");

					// add triangle from vertices
					tris.add(new Triangle(verts.get(Integer.valueOf(pX[0]) - 1), verts.get(Integer.valueOf(pY[0]) - 1),
							verts.get(Integer.valueOf(pZ[0]) - 1)));
				}
			}

			// close the scanner
			scnr.close();
			verts = null;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * If ray hits bounding box, check collision with all triangles
	 * 
	 * @param o origin of the ray
	 * @param d direction of ray
	 * @return true if there is a collision
	 */
	public boolean intersect(Vector3 o, Vector3 d, Vector3 hit, Vector3 tuv, Vector3 n) {

		// check intersection with bounding box
		double txmin = (xMin - o.getX()) / d.getX();
		double txmax = (xMax - o.getX()) / d.getX();
		double tymin = (yMin - o.getY()) / d.getY();
		double tymax = (yMax - o.getY()) / d.getY();
		double tzmin = (zMin - o.getZ()) / d.getZ();
		double tzmax = (zMax - o.getZ()) / d.getZ();

		double tmin = Math.max(Math.max(Math.min(txmin, txmax), Math.min(tymin, tymax)), Math.min(tzmin, tzmax));
		double tmax = Math.min(Math.min(Math.max(txmin, txmax), Math.max(tymin, tymax)), Math.max(tzmin, tzmax));

		if ((tmax < 0) || (tmin > tmax)) {
			return false;
		}

		// intersection data to return
		double dist = Double.MAX_VALUE;
		Vector3 hitRet = new Vector3(0, 0, 0);
		Vector3 tuvRet = new Vector3(0, 0, 0);
		Vector3 nRet = new Vector3(0, 0, 0);
		boolean noHit = true;

		// check intersection with triangles
		for (int i = 0; i < tris.size(); i++) {
			if (tris.get(i).MTint(o, d, hit, tuv)) {
				// there is an intersection
				noHit = false;
				// find closest triangle
				if (tuv.getX() < dist) {
					// save triangle info
					dist = tuv.getX();
					hitRet.set(hit);
					tuvRet.set(tuv);
					nRet.set(tris.get(i).n);
				}
			}
		}

		// return triangle info
		if (noHit) {
			return false;
		} else {
			hit.set(hitRet);
			tuv.set(tuvRet);
			n.set(nRet);
			return true;
		}

	}

}
