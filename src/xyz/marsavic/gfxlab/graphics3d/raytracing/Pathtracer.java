package xyz.marsavic.gfxlab.graphics3d.raytracing;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.graphics3d.cameras.Perspective;
import xyz.marsavic.gfxlab.graphics3d.cameras.TransformedCamera;
import xyz.marsavic.random.sampling.Sampler;
import xyz.marsavic.utils.Hashing;

import java.util.Random;


public class Pathtracer extends Raytracer {
	
	private static final double EPSILON = 1e-9;
	private static final long seed = 0x68EFD508E309A865L;
	
	private final int maxDepth;

	private double dubina;
	
	public Pathtracer(Scene scene, Camera camera, int maxDepth, int dubina) {
		super(scene, camera);
		this.maxDepth = maxDepth;
		this.dubina = dubina;
	}
	
	public Pathtracer(Scene scene, Camera camera) {
		this(scene, camera, 16, 0);
	}
	
	
	@Override
	protected Color sample(Ray ray) {
		return radiance(ray, maxDepth, new Sampler(Hashing.mix(seed, ray)));
	}
	
	
	private Color radiance(Ray ray, int depthRemaining, Sampler sampler) {
		if (depthRemaining <= 0) return Color.BLACK;
		
		Hit hit = scene.solid().firstHit(ray, EPSILON);

		if(depthRemaining == maxDepth){
			double d1 = ray.at(hit.t()).z();
			double k = Math.abs(d1 - dubina) * 0.002;

			ray = Ray.pd(ray.p(), ray.d().add(Vec3.xyz(sampler.rng().nextDouble(-k, k), sampler.rng().nextDouble(-k, k), sampler.rng().nextDouble(-k, k))));
			hit = scene.solid().firstHit(ray, hit.t()-2*k);
		}

		if (hit.t() == Double.POSITIVE_INFINITY) {
			return scene.colorBackground();
		}

		Material material = hit.material();
		Color result = material.emittance();
		
		Vec3 i = ray.d().inverse();                 // Incoming direction
		Vec3 n_ = hit.n_();                         // Normalized normal to the body surface at the hit point
		BSDF.Result bsdfResult = material.bsdf().sample(sampler, n_, i);
		
		if (bsdfResult.color().notZero()) {
			Vec3 p = ray.at(hit.t());               // Point of collision
			Ray rayScattered = Ray.pd(p, bsdfResult.out());
			Color rO = radiance(rayScattered, depthRemaining - 1, sampler);
			Color rI = rO.mul(bsdfResult.color());
			result = result.add(rI);
		}
		
		return result;
	}
	
}
