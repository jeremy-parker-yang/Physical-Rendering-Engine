/**
 * Get contribution of sky light from a given direction
 * 
 * @author Jeremy Parker Yang
 *
 */
public class SkyLight {

	public SkyLight() {

	}

	/**
	 * Calculates the light contribution from this direction. Uses spherical
	 * polar coordinates where phi is the angle from x towards y and theta is
	 * the angle from z towards x.
	 * 
	 * @param d direction in cartesian coordinates
	 * @return contribution from light
	 */
	public static double getLight(Vector3 d) {

		// this is an arbitrary function that could be changed later
		Vector3 a = d.norm().sub(new Vector3(-0.2, .2, .7).norm());
		if (a.dot(a) > .05) {
			return .15;
		}
		return 15;
	}
}
