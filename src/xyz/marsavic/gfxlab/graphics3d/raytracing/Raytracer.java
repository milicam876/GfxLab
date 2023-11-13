package xyz.marsavic.gfxlab.graphics3d.raytracing;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunctionT;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Scene;


public abstract class Raytracer implements ColorFunctionT {
	
	protected final Scene scene;
	
	
	public Raytracer(Scene scene) {
		this.scene = scene;
	}
	
	
	protected abstract Color sample(Ray ray);
	
	
	@Override
	public Color at(double t, Vector p) {
		Ray ray = Ray.pd(Vec3.ZERO, Vec3.zp(1, p));
		return sample(ray);
	}
	
}
