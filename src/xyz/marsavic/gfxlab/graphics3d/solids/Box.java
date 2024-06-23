package xyz.marsavic.gfxlab.graphics3d.solids;


import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.BoxedObjectFactory;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.random.sampling.Sampler;
import xyz.marsavic.utils.Numeric;

import java.util.Iterator;


public class Box extends Solid implements Iterable<Vec3> {
	
	public static final BoxedObjectFactory.PQ<Box> $ = Box::new;
	public static Box UNIT = Box.$.pq(Vec3.ZERO, Vec3.EXYZ);
	
	
	private final Vec3 p, q;
	private final F1<Material, Vector> mapMaterial;
	
	
	
	private Box(Vec3 p, Vec3 q, F1<Material, Vector> mapMaterial) {
		this.p = p;
		this.q = q;
		this.mapMaterial = mapMaterial;
		this.boundingBox = BoundingBox.$.b(this);
	}
	
	private Box(Vec3 p, Vec3 q) {
		this(p, q, Material.DEFAULT);
	}
	
	public Box material(F1<Material, Vector> mapMaterial) {
		return new Box(p, q, mapMaterial);
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
		return o.sub(p).sign().sub(q.sub(o).sign()).allZero();
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
			// TODO Test performance if we change to lazy n computation storing only i in HitBox.
			if (max0 > afterTime) return new HitBox(max0, Vec3.E[iMax0].mul(-Numeric.sign(ray.d().get(iMax0))));
			if (min1 > afterTime) return new HitBox(min1, Vec3.E[iMin1].mul( Numeric.sign(ray.d().get(iMin1))));
		}
		return Hit.AtInfinity.axisAlignedGoingIn(ray.d());
	}

	//@Override
	public boolean intersects(BoundingBox boundingBox) {
		return intervalsIntersect(this.p.x(), this.q.x(), boundingBox.p().x(), boundingBox.q().x())
				&& intervalsIntersect(this.p.y(), this.q.y(), boundingBox.p().y(), boundingBox.q().y())
				&& intervalsIntersect(this.p.z(), this.q.z(), boundingBox.p().z(), boundingBox.q().z());
	}

	private boolean intervalsIntersect(double min1, double max1, double min2, double max2){
		return !(max1 < min2 || max2 < min1);
	}

	public Vec3 random(Sampler sampler) {
		return Vec3.xyz(
				Numeric.interpolateLinear(p().x(), q().x(), sampler.uniform()),
				Numeric.interpolateLinear(p().y(), q().y(), sampler.uniform()),
				Numeric.interpolateLinear(p().z(), q().z(), sampler.uniform())
		);
	}
	
	@Override
	public Iterator<Vec3> iterator() {
		return new GridIterator(this);
	}
	
	
	/**
	 * Iterates through points inside the specified box, starting from the corner p.
	 */
	public static class GridIterator implements Iterator<Vec3> {
		private final Vec3 p, step;
		
		private final int dX, dY, dZ;
		
		private int iNextX, iNextY, iNextZ;
		private boolean hasNext;
		
		
		
		public GridIterator(Box b, Vec3 step) {
			if (!step.sign().equals(b.d().sign())) {
				throw new IllegalArgumentException("b.d and step must have the same direction in all coordinates.");
			}
			
			this.step = step;
			p = b.p();
			Vec3 d = b.d().div(step).abs().ceil();
			dX = (int) d.x();
			dY = (int) d.y();
			dZ = (int) d.z();
			hasNext = d.product() > 0;
		}
		
		
		public GridIterator(Vec3 d, Vec3 step) {
			this(Box.$.pd(Vec3.ZERO, d), step);
		}
		
		
		public GridIterator(Vec3 d) {
			this(d, d.sign());
		}
		
		
		public GridIterator(Box b) {
			this(b, b.d().sign());
		}
		
		
		@Override
		public boolean hasNext() {
			return hasNext;
		}
		
		
		@Override
		public Vec3 next() {
			Vec3 pos = Vec3.xyz(iNextX, iNextY, iNextZ).mul(step).add(p);
			
			if (++iNextX < dX) return pos;
			iNextX = 0;
			if (++iNextY < dY) return pos;
			iNextY = 0;
			hasNext = (++iNextZ < dZ);
			return pos;
		}
	}
	
	
	final class HitBox implements Hit {
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
		public Material material() {
			return Box.this.mapMaterial.at(uv());
		}
		
		@Override
		public Vector uv() {
			return Vector.ZERO; // TODO
		}
		
		@Override
		public Hit inverted() {
			return new HitBox(t, n_.inverse());
		}
		
	}
	
}
