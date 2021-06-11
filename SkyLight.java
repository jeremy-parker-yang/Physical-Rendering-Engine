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
		//-0.2, .2, .7
		//-0.7, .9, .75
		Vector3 a = d.norm().sub(new Vector3(-0.2, .9, .7).norm()); //y = .2
		if (a.dot(a) > .1) {
			return .12;
		}
		return 300; //15
	}
}
