package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.BSDF;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.random.sampling.Sampler;
import xyz.marsavic.utils.Numeric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChristmasTree extends Scene.Base {

	
	private Ball ornament(Sampler sampler, Vec3 p, boolean light) {
		F1<Material, Vector> materialMap;
		
		if (light || sampler.uniform() < 0.2) {
			Color color = Color.hsb(0, sampler.uniform(0.7), 1.0);
			Material mA = new Material(BSDF.TRANSMISSIVE);
			Material mB = Material.light(color);
			Vector v = sampler.randomGaussian(4).roundAwayFromZero();
			materialMap = c -> Numeric.mod(c.dot(v)) < 0.1 ? mB : mA;
		} else {
			Color color = Color.hsb(.33, sampler.uniform(0.6, 0.8), 1);
			materialMap = new Material(BSDF.glossy(color, sampler.uniform(0.5, 1)));
		}
		
		return Ball.cr(p, 1, materialMap);
	}
	
	
	public ChristmasTree() {
		Sampler sampler = new Sampler(0x92138E3FL);
		
		Collection<Solid> solids = new ArrayList<>();
		
		solids.add(HalfSpace.pn(Vec3.xyz(0, -2, 0), Vec3.xyz(0, 1, 0), Material.matte(Color.hsb(0.83, 0.4, 0.7))));
		
		int n = 56;
		double r = 10;
		double h = 20;
		
		List<Vec3> centers = new ArrayList<>();
		
		while (centers.size() < n) {
			double y = sampler.uniform();
			Vec3 p = Vec3.yp(h * (1 - y), Vector.polar(sampler.uniform(r) * Math.pow(y, 1), sampler.uniform()));
			
			if (centers.stream().allMatch(c -> c.sub(p).length() > 3)) {
				centers.add(p);
				solids.add(ornament(sampler, p, y < 0.1));
			}
		}
		
		solid = Group.of(solids);
	}
}

