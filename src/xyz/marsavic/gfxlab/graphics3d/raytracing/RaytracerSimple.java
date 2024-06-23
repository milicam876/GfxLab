package xyz.marsavic.gfxlab.graphics3d.raytracing;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;


public class RaytracerSimple extends Raytracer {
	
	private final static double EPSILON = 1e-9;
	
	public RaytracerSimple(Scene scene, Camera camera) {
		super(scene, camera);
	}
	
	@Override
	protected Color sample(Ray ray) {
		return sample(ray, 64);
	}
	
	protected Color sample(Ray ray, int depthRemaining) {
		if (depthRemaining <= 0) {
			return Color.BLACK;
		}
		
		Hit hit = scene.solid().firstHit(ray, EPSILON);
		
		if (hit.t() == Double.POSITIVE_INFINITY) {
			return scene.colorBackground();
		}
		
		Vec3 p  = ray.at(hit.t());                  // The hit point
		Vec3 n_ = hit.n_();                         // Normalized normal to the body surface at the hit point
		Vec3 i_ = ray.d().inverse().normalized_();  // Incoming direction
		Vec3 r_ = GeometryUtils.reflectedN(n_, i_); // Reflected ray (i_ reflected over n_)
		
		Material material = hit.material();
		
		Color lightDiffuse  = Color.BLACK;          // The sum of diffuse contributions from all the lights
		Color lightSpecular = Color.BLACK;
		
		for (Light light : scene.lights()) {
			Vec3 l = light.p().sub(p);              // Vector from p to the light;
			Ray rayToLight = Ray.pd(p, l);
			if (scene.solid().hitBetween(rayToLight, EPSILON, 1)) {
				continue;
			}
			
			double lLSqr = l.lengthSquared();       // Distance from p to the light squared
			double lL = Math.sqrt(lLSqr);           // Distance from p to the light
			double cosLN = n_.dot(l) / lL;          // Cosine of the angle between l and n_
			
			if (cosLN > 0) {                        // If the light is above the surface
				Color irradiance = light.c().mul(cosLN / lLSqr);
				// The irradiance represents how much light is received by a unit area of the surface. It is
				// proportional to the cosine of the incoming angle and inversely proportional to the distance squared
				// (inverse-square law).
				lightDiffuse = lightDiffuse.add(irradiance);
				
				double cosLR = l.dot(r_);
				if (cosLR > 0) {                   // If the angle between l and r is acute
					cosLR /= lL;
					lightSpecular = lightSpecular.add(irradiance.mul(Math.pow(cosLR, material.shininess())));
				}
			}
		}
		
		Color result = Color.BLACK;
		result = result.add(material.diffuse ().mul(lightDiffuse ));
		result = result.add(material.specular().mul(lightSpecular));
		
		if (material.reflective().notZero()) {   // When material has reflective properties we recursively find the color visible along the ray (p, r).
			Color lightReflected = sample(Ray.pd(p, r_), depthRemaining - 1);
			result = result.add(material.reflective().mul(lightReflected));
		}
		
		if (material.refractive().notZero()) {   // When material has refractive properties we recursively find the color visible along the ray (p, f).
			Vec3 f = GeometryUtils.refractedNN(n_, i_, material.refractiveIndex());
			Color lightRefracted = sample(Ray.pd(p, f), depthRemaining - 1);
			result = result.add(lightRefracted).mul(material.refractive());
		}
		
		return result;
	}
}
