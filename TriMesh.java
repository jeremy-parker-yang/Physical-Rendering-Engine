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

	public ArrayList<Triangle> tris = new ArrayList<Triangle>();

	/**
	 * Construct mesh from .obj file format. Object must be triangulated and in a
	 * right-hand coordinate system.
	 * 
	 * @param fileName obj file to read
	 */
	public TriMesh(String fileName) {
		ArrayList<Vector3> verts = new ArrayList<Vector3>();
		try {

			// set up file scanner
			Scanner scnr = new Scanner(new File(fileName));
			String[] line;
			String[] pA = new String[1];
			String[] pB = new String[1];
			String[] pC = new String[1];

			// for each line
			while (scnr.hasNextLine()) {
				
				line = scnr.nextLine().split(" ");
				
				// add vertex
				if (line[0].equals("v")) {
					verts.add(new Vector3(Double.valueOf(line[1]), 
							Double.valueOf(line[2]), Double.valueOf(line[3])));
				}

				// add triangle
				else if (line[0].equals("f")) {

					// get vertex info only
					pA = line[1].split("/");
					pB = line[2].split("/");
					pC = line[3].split("/");
					
					// add triangle from vertices
					tris.add(new Triangle(verts.get(Integer.valueOf(pA[0])-1), 
							verts.get(Integer.valueOf(pB[0])-1),
							verts.get(Integer.valueOf(pC[0])-1)));
				}
			}

			// close the scanner
			scnr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
