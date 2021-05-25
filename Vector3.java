/**
 * Simple 3D vector math class
 * 
 * @author Jeremy Parker Yang
 *
 */
public class Vector3 {
	private double x;
	private double y;
	private double z;

	/**
	 * Construct vector
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Set as a copy of vector a
	 */
	public void set(Vector3 a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
	}

	/**
	 * Gets x component
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Gets y component
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Gets z component
	 */
	public double getZ() {
		return this.z;
	}

	/**
	 * Vector addition
	 */
	Vector3 add(Vector3 a) {
		return new Vector3(this.x + a.x, this.y + a.y, this.z + a.z);
	}

	/**
	 * Vector subtraction
	 */
	Vector3 sub(Vector3 a) {
		return new Vector3(this.x - a.x, this.y - a.y, this.z - a.z);
	}

	/**
	 * Vector scaling by constant
	 */
	Vector3 mul(double a) {
		return new Vector3(a * this.x, a * this.y, a * this.z);
	}

	/**
	 * Vector scaling by vector
	 */
	Vector3 mul(Vector3 a) {
		return new Vector3(a.x * this.x, a.y * this.y, a.z * this.z);
	}

	/**
	 * Left multiplies a vector by a 3x3 transformation matrix
	 */
	Vector3 mul(double[][] mat) {
		return new Vector3(mat[0][0] * x + mat[0][1] * y + mat[0][2] * z, mat[1][0] * x + mat[1][1] * y + mat[1][2] * z,
				mat[2][0] * x + mat[2][1] * y + mat[2][2] * z);
	}

	/**
	 * Dot Product
	 */
	double dot(Vector3 a) {
		return this.x * a.x + this.y * a.y + this.z * a.z;
	}

	/**
	 * Cross Product
	 */
	Vector3 cross(Vector3 a) {
		return new Vector3(this.y * a.z - this.z * a.y, this.z * a.x - this.x * a.z, this.x * a.y - this.y * a.x);
	}

	/**
	 * Normalize vector
	 */
	Vector3 norm() {
		return this.mul(1d / Math.sqrt(this.dot(this)));
	}

	/**
	 * Generates a rotation matrix to rotate a single point. Uses Euler ZYX format.
	 * 
	 * @param rot x component is gamma, y is beta, z is alpha.
	 * @return rotation matrix
	 */
	public static double[][] getRotMat(Vector3 rot) {
		
		// calculate cosines of rot vector
		double cA = Math.cos(rot.getZ());
		double cB = Math.cos(rot.getY());
		double cY = Math.cos(rot.getX());

		// calculate sines of rot vector
		double sA = Math.sin(rot.getZ());
		double sB = Math.sin(rot.getY());
		double sY = Math.sin(rot.getX());

		// rotation matrix
		double[][] rotMat = new double[3][3];

		// vector 0
		rotMat[0][0] = cA * cB;
		rotMat[1][0] = sA * cB;
		rotMat[2][0] = -sB;

		// vector 1
		rotMat[0][1] = (cA * sB * sY) - (sA * cY);
		rotMat[1][1] = (sA * sB * sY) + (cA * cY);
		rotMat[2][1] = cB * sY;

		// vector 2
		rotMat[0][2] = (cA * sB * cY) + (sA * sY);
		rotMat[1][2] = (sA * sB * cY) - (cA * sY);
		rotMat[2][2] = cB * cY;

		return rotMat;
	}
}
