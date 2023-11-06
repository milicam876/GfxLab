package xyz.marsavic.gfxlab.graphics3d.raytracing;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunctionT;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.utils.Numeric;


public class RaytracerTest implements ColorFunctionT {
	
	@Override
	public Color at(double t, Vector p) {
		Ball ball = Ball.cr(Vec3.xyz(Numeric.sinT(t), 0, 3 + Numeric.cosT(t)), 1);
		HalfSpace floor = HalfSpace.pn(Vec3.xyz(0, -1, 3), Vec3.xyz(0, 1, 0));
		
		Ray ray = Ray.pd(Vec3.ZERO, Vec3.zp(1, p));
		
		Hit hitB = ball.firstHit(ray);
		Hit hitF = floor.firstHit(ray);
		
		double tHit = Math.min(hitB.t(), hitF.t());
		
		return Color.gray(1 / (1 + tHit));
	}
	
}
