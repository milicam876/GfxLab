package xyz.marsavic.gfxlab.graphics3d.cameras;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Camera;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.utils.Numeric;


public record Perspective (
		double k
) implements Camera {
	
	public Perspective() {
		this(1.0);
	}
	
	
	public static Camera fov(double angle) {
		return new Perspective(Numeric.tanT(angle / 2));
	}
	
	
	@Override
	public Ray exitingRay(Vector p) {
		return Ray.pd(Vec3.ZERO, Vec3.zp(1 / k, p));
	}
	
}
