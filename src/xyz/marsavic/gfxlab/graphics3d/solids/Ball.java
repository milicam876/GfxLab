package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;


public class Ball implements Solid {
	
	private final Vec3 c;
	private final double r;
	private final boolean inverted;
	
	// transient
	private final double rSqr;
	
	
	/** Negative r will make the ball inverted (the resulting solid is a complement of a ball). */
	private Ball(Vec3 c, double r) {
		this.c = c;
		this.r = r;
		rSqr = r * r;
		inverted = r < 0;
	}
	
	
	public static Ball cr(Vec3 c, double r) {
		return new Ball(c, r);
	}
	
	
	public Vec3 c() {
		return c;
	}
	
	
	public double r() {
		return r;
	}
	
	
	
	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		Vec3 e = c().sub(ray.p());                                // Vector from the ray origin to the ball center
		
		double dSqr = ray.d().lengthSquared();
		double l = e.dot(ray.d()) / dSqr;
		double mSqr = l * l - (e.lengthSquared() - rSqr) / dSqr;
		
		if (mSqr > 0) {
			double m = Math.sqrt(mSqr);
			if (l - m > afterTime) return new HitBall(ray, l - m);
			if (l + m > afterTime) return new HitBall(ray, l + m);
		}
		return Hit.AtInfinity.axisAligned(ray.d(), inverted);
	}
	
	
	class HitBall extends Hit.RayT {
		
		protected HitBall(Ray ray, double t) {
			super(ray, t);
		}
		
		@Override
		public Vec3 n() {
			return ray().at(t()).sub(c());
		}
		
		@Override
		public Vec3 n_() {
			return n().div(r);
		}
		
	}
	
}
