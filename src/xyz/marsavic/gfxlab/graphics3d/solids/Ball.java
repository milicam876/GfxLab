package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Transformation;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.utils.Numeric;


public class Ball extends Solid {
	
	private final Vec3 c;
	private final double r;
	private final boolean inverted;
	private final F1<Material, Vector> mapMaterial;
	
	// transient
	private final double rSqr;
	
	
	/** Negative r will make the ball inverted (the resulting solid is a complement of a ball). */
	private Ball(Vec3 c, double r, F1<Material, Vector> mapMaterial) {
		this.c = c;
		this.r = r;
		this.mapMaterial = mapMaterial;
		rSqr = r * r;
		inverted = r < 0;
		this.boundingBox = BoundingBox.$.cr(c, Vec3.xyz(r,r,r));
	}
	
	
	public static Ball cr(Vec3 c, double r, F1<Material, Vector> mapMaterial) {
		return new Ball(c, r, mapMaterial);
	}
	
	public static Ball cr(Vec3 c, double r) {
		return cr(c, r, Material.DEFAULT);
	}
	
	
	public Vec3 c() {
		return c;
	}
	
	
	public double r() {
		return r;
	}
	
	

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

	public boolean intersects(BoundingBox boundingBox) {
		//transliramo se u prvi oktant
		Vec3 bq = boundingBox.r();
		Vec3 c = this.c().sub(boundingBox.c()).abs();

		double cDistanceSq;

		if(c.x() <= bq.x() && c.y() <= bq.y() && c.z() <= bq.z()){
			return true;
		}
		else if(c.x() <= bq.x()){
			if (c.y() <= bq.y()){
				cDistanceSq = c.z()-bq.z();
				cDistanceSq *= cDistanceSq;
			}
			else if (c.z() <= bq.z()){
				cDistanceSq = c.y()-bq.y();
				cDistanceSq *= cDistanceSq;
			}
			else {
				double d1 = c.z()-bq.z();
				double d2 = c.y()-bq.y();
				d1 *= d1;
				d2 *= d2;
				cDistanceSq = d1+d2;
			}
		}
		else if (c.y() <= bq.y()){
			if (c.z() <= bq.z()){
				cDistanceSq = c.x()-bq.x();
				cDistanceSq *= cDistanceSq;
			}
			else {
				double d1 = c.z()-bq.z();
				double d2 = c.x()-bq.x();
				d1 *= d1;
				d2 *= d2;
				cDistanceSq = d1+d2;
			}
		}
		else if (c.z() <= bq.z()) {
			double d1 = c.y()-bq.y();
			double d2 = c.x()-bq.x();
			d1 *= d1;
			d2 *= d2;
			cDistanceSq = d1+d2;
		}
		else {
			double d1 = c.x()-bq.x();
			double d2 = c.y()-bq.y();
			double d3 = c.z()-bq.z();
			d1 *= d1;
			d2 *= d2;
			d3 *= d3;
			cDistanceSq = d1 + d2 + d3;
		} //prodje mi mladost dok sam napisala sve slucajeve

		return cDistanceSq < r*r;
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
		public Material material() {
			return Ball.this.mapMaterial.at(uv());
		}
		
		@Override
		public Vector uv() {
			Vec3 n = n();
			return Vector.xy(
					Numeric.atan2T(n.z(), n.x()),
					4 * Numeric.asinT(n.y() / r)
			);
		}
		
		@Override
		public Vec3 n_() {
			return n().div(r);
		}
		
	}
	
}
