package xyz.marsavic.gfxlab.graphics3d;


import xyz.marsavic.gfxlab.Vec3;


public class GeometryUtils {
	
	/** An orthogonal vector to v. */
	public static Vec3 normal(Vec3 v) {
		if (v.x() != 0 || v.y() != 0) {
			return Vec3.xyz(-v.y(), v.x(), 0);
		} else {
			return Vec3.EX;
		}
	}
	
	
	public static Vec3 reflected(Vec3 n, Vec3 i) {
		return n.mul(i.dot(n) * 2 / n.lengthSquared()).sub(i);
	}

	public static Vec3 reflectedN(Vec3 n_, Vec3 i) {
		return n_.mul(i.dot(n_) * 2).sub(i);
	}
	
	
	public static Vec3 refractedNN(Vec3 n_, Vec3 i_, double refractiveIndex) {
		double c1 = i_.dot(n_);
		double ri = c1 >= 0 ? refractiveIndex : -1.0 / refractiveIndex;
		double c2Sqr = 1 - (1 - c1 * c1) / (ri * ri);
		
		return c2Sqr > 0 ?
				n_.mul(c1 - Math.sqrt(c2Sqr) * ri).sub(i_) :    // refraction
				reflectedN(n_, i_);                             // total reflection
	}
	
}
