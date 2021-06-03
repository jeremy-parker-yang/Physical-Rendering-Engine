/**
 * Shader to define physical properties of a material.
 * 
 * @author Jeremy Parker Yang
 *
 */
public class Material {

//	private final double roughness;
//	private final double reflectance;
//	private final int colorR;
//	private final int colorG;
//	private final int colorB;

	public Material() {

	}

	/**
	 * Light scattering is random and follows a cosine distribution.
	 * 
	 * @param n  normal of surface
	 * @param t1 tangent 1 of surface
	 * @param t2 tangent 2 of surface
	 * @return the direction of reflection
	 */
	public static Vector3 scatter(Vector3 n, Vector3 t1, Vector3 t2) {

		// generate direction from cosine distribution
		double sin = Math.sqrt(Math.random());
		double cos = Math.sqrt(1 - sin * sin);
		double phi = 2 * Math.PI * Math.random();

		Vector3 v1 = n.mul(cos);
		Vector3 v2 = t1.mul(sin * Math.cos(phi));
		Vector3 v3 = t2.mul(sin * Math.sin(phi));

		return v1.add(v2).add(v3).norm();
	}
}
