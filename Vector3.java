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
	 * Vector scaling
	 */
	Vector3 mul(double a) {
		return new Vector3(a * this.x, a * this.y, a * this.z);
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
}
