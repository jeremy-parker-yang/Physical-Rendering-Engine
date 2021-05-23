/**
 * Triangle defined as one point and two vector edges. Assumes a right-handed
 * coordinate system.
 * 
 * @author Jeremy Parker Yang
 *
 */
public class Triangle {
	Vector3 a; // point a
	Vector3 e1; // edge 1
	Vector3 e2; // edge 1

	// TODO: local coords
	Vector3 n; // normal
	// Vector3 t1; // tangent 1
	// Vector3 t2; // tangent 2

	/**
	 * Construct triangle from 3 vertices
	 * 
	 * @param a Cartesian coords vertex a
	 * @param b Cartesian coords vertex b
	 * @param c Cartesian coords vertex c
	 */
	public Triangle(Vector3 a, Vector3 b, Vector3 c) {
		// defines triangle
		this.a = a;
		e1 = b.sub(a);
		e2 = c.sub(a);

		// defines local coord system
		n = e1.cross(e2).norm();
		// t1
		// t2
	}

	/**
	 * Moller-Trumbore algorithm for ray-triangle intersection
	 * 
	 * @param o origin of the ray
	 * @param d direction of ray
	 * @return true if there is a hit
	 * @return hit point of intersection
	 * @return tuv Barycentric coords of intersection
	 */
	public boolean MTint(Vector3 o, Vector3 d, Vector3 hit, Vector3 tuv) {

		// back facing triangles do not intersect
		if (d.dot(n) > 0)
			return false;

		Vector3 t = o.sub(a);
		Vector3 p = d.cross(e2);
		Vector3 q = t.cross(e1);

		double k = 1d / p.dot(e1);
		double u = k * p.dot(t);

		// check intersection
		if (u < 0 || u > 1)
			return false;

		double v = k * q.dot(d);

		// check intersection
		if (v < 0 || u + v > 1)
			return false;

		// get point of intersection
		tuv.set(new Vector3(k * q.dot(e2), u, v));
		hit.set(o.add(d.mul(tuv.getX())));

		return true;
	}
}
