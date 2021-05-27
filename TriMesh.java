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
	 * Default constructor. Construct mesh exactly from .obj file. Object must
	 * be triangulated and in a right-hand coordinate system.
	 * 
	 * @param fileName .obj file to read
	 */
	public TriMesh(String fileName) {
		// store verts to generate tris
		ArrayList<Vector3> verts = new ArrayList<Vector3>();
		double x = 0, y = 0, z = 0; // store a single vertex
		String[] line = null; // store a line from the file

		// read data from file
		try {
			// set up file scanner
			Scanner scnr = new Scanner(new File(fileName));

			// add vertices
			while (scnr.hasNextLine()) {

				// read entire line
				line = scnr.nextLine().split(" ");

				// handle vertex
				if (line[0].equals("v")) {
					x = Double.valueOf(line[1]);
					y = Double.valueOf(line[2]);
					z = Double.valueOf(line[3]);
					verts.add(new Vector3(x, y, z));

					// adjust bounding box
					// x component
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

				// handle triangle
				else if (line[0].equals("f")) {
					// exit loop once triangle section starts
					break;
				}
			}

			// add triangles
			addTris(verts, scnr, line);

			// close scanner
			scnr.close();
			verts = null;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct mesh considering rotation, translation, and scaling. Object
	 * must be triangulated and in a right-hand coordinate system.
	 * 
	 * @param fileName obj file to read
	 * @param rot      xyz rotation angles
	 * @param scale    xyz scale constants
	 * @param trans    xyz translation constants
	 */
	public TriMesh(String fileName, Vector3 scale, Vector3 rot, Vector3 trans) {
		// store verts to generate tris
		ArrayList<Vector3> verts = new ArrayList<Vector3>();
		String[] line = null; // store a line from the file

		// data for transformations
		Vector3 transformed = new Vector3(0, 0, 0);
		double[][] rotMat = Vector3.getRotMat(rot);

		// read data from file
		try {
			// set up file scanner
			Scanner scnr = new Scanner(new File(fileName));

			// add vertices
			while (scnr.hasNextLine()) {

				// read entire line
				line = scnr.nextLine().split(" ");

				// handle vertex
				if (line[0].equals("v")) {

					// get original vertex
					transformed = new Vector3(Double.valueOf(line[1]),
							Double.valueOf(line[2]), Double.valueOf(line[3]));

					// apply transforms: scale, rotate, translate
					transformed = transformed.mul(scale);
					transformed = transformed.mul(rotMat);
					transformed = transformed.add(trans);

					// add transformed point
					verts.add(transformed);

					// adjust bounding box
					// x component
					if (transformed.getX() > xMax) {
						xMax = transformed.getX();
					} else if (transformed.getX() < xMin) {
						xMin = transformed.getX();
					}
					// y component
					if (transformed.getY() > yMax) {
						yMax = transformed.getY();
					} else if (transformed.getY() < yMin) {
						yMin = transformed.getY();
					}
					// z component
					if (transformed.getZ() > zMax) {
						zMax = transformed.getZ();
					} else if (transformed.getZ() < zMin) {
						zMin = transformed.getZ();
					}
				}

				// handle triangle
				else if (line[0].equals("f")) {
					// exit loop once triangle section starts
					break;
				}
			}

			// add faces
			addTris(verts, scnr, line);

			// close the scanner
			scnr.close();
			verts = null;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Ray-mesh intersection. (uses bounding box)
	 * 
	 * @param o origin of the ray
	 * @param d direction of ray
	 * @return true if there is a collision
	 */
	public boolean meshInt(Vector3 o, Vector3 d, Vector3 hit, Vector3 tuv,
			Vector3 n, Vector3 t1, Vector3 t2) {

		// check intersection with bounding box
		double txmin = (xMin - o.getX()) / d.getX();
		double txmax = (xMax - o.getX()) / d.getX();
		double tymin = (yMin - o.getY()) / d.getY();
		double tymax = (yMax - o.getY()) / d.getY();
		double tzmin = (zMin - o.getZ()) / d.getZ();
		double tzmax = (zMax - o.getZ()) / d.getZ();

		double tmin = Math.max(
				Math.max(Math.min(txmin, txmax), Math.min(tymin, tymax)),
				Math.min(tzmin, tzmax));
		double tmax = Math.min(
				Math.min(Math.max(txmin, txmax), Math.max(tymin, tymax)),
				Math.max(tzmin, tzmax));

		if ((tmax < 0) || (tmin > tmax)) {
			return false;
		}

		// store intersection data
		double dist = Double.MAX_VALUE;
		Vector3 hitRet = new Vector3(0, 0, 0);
		Vector3 tuvRet = new Vector3(0, 0, 0);

		// check intersection with triangles
		int ihit = 0; // triangle that intersects
		for (int i = 0; i < tris.size(); i++) {
			// there is an intersection
			if (tris.get(i).MTint(o, d, hit, tuv)) {
				// find closest triangle
				if (tuv.getX() < dist) {
					// save collsion specific info
					dist = tuv.getX();
					hitRet.set(hit);
					tuvRet.set(tuv);
					ihit = i;
				}
			}
		}

		// return triangle and collision info
		if (dist == Double.MAX_VALUE) {
			return false;
		} else {
			n.set(tris.get(ihit).n);
			t1.set(tris.get(ihit).t1);
			t2.set(tris.get(ihit).t2);
			hit.set(hitRet);
			tuv.set(tuvRet);
			return true;
		}
	}

	/**
	 * Helper method for constructor. Adds triangles to mesh.
	 * 
	 * @param verts add vertices here
	 * @param scnr  scan through file
	 * @param line  previous line read
	 */
	private void addTris(ArrayList<Vector3> verts, Scanner scnr,
			String[] line) {
		// x y z parts of data
		String[] pX = new String[1];
		String[] pY = new String[1];
		String[] pZ = new String[1];

		do {
			// add triangle
			if (line[0].equals("f")) {

				// get vertex info only
				pX = line[1].split("/");
				pY = line[2].split("/");
				pZ = line[3].split("/");

				// add triangle from vertices
				tris.add(new Triangle(verts.get(Integer.valueOf(pX[0]) - 1),
						verts.get(Integer.valueOf(pY[0]) - 1),
						verts.get(Integer.valueOf(pZ[0]) - 1)));

				line = scnr.nextLine().split(" ");
			}

		} while (scnr.hasNextLine());
	}
}