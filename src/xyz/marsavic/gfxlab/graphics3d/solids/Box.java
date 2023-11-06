package xyz.marsavic.gfxlab.graphics3d.solids;


import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.BoxedObjectFactory;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.utils.Numeric;


public class Box implements Solid {
	
	public static final BoxedObjectFactory.PQ<Box> $ = Box::new;
	public static Box UNIT = Box.$.pq(Vec3.ZERO, Vec3.EXYZ);
	
	
	private final Vec3 p, q;
	

	
	private Box(Vec3 p, Vec3 q) {
		this.p = p;
		this.q = q;
	}
	
	
	public Vec3 p() {
		return p;
	}

	
	public Vec3 q() {
		return q;
	}
	
	
	public Vec3 d() {
		return q.sub(p);
	}
	
	
	public Vec3 c() {
		return p.add(q).div(2);
	}
	
	
	public Vec3 r() {
		return d().div(2);
	}
	
	
	public boolean contains(Vec3 o) {
		return o.sub(p).sign().dot(o.sub(q).sign()) == -3;
	}
	
	
	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		Vec3 tP = p().sub(ray.p()).div(ray.d());
		Vec3 tQ = q().sub(ray.p()).div(ray.d());
		
		Vec3 t0 = Vec3.min(tP, tQ);
		Vec3 t1 = Vec3.max(tP, tQ);
		
		int iMax0 = t0.maxIndex();
		int iMin1 = t1.minIndex();
		
		double max0 = t0.get(iMax0);
		double min1 = t1.get(iMin1);
		
		if (max0 < min1) {
			if (max0 > afterTime) return new HitBox(max0, Vec3.E[iMax0].mul(-Numeric.sign(ray.d().get(iMax0))));
			if (min1 > afterTime) return new HitBox(min1, Vec3.E[iMin1].mul( Numeric.sign(ray.d().get(iMin1))));
		}
		return Hit.AtInfinity.axisAlignedOut(ray.d());
	}
	
	
	static final class HitBox implements Hit {
		private final double t;
		private final Vec3 n_;
		

		HitBox(double t, Vec3 n_) {
			this.t = t;
			this.n_ = n_;
		}
		
		@Override public double t() { return t; }
		@Override public Vec3 n_() { return n_; }
		@Override public Vec3 n() { return n_; }
		
		@Override
		public Hit inverted() {
			return new HitBox(t, n_.inverse());
		}
		
	}
	
	
}
