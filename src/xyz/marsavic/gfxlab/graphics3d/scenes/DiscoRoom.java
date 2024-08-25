package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Light;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.*;
import xyz.marsavic.random.RNG;

import java.util.ArrayList;
import java.util.Collection;


public class DiscoRoom extends Scene.Base {
	
	public DiscoRoom(int nBalls, int nLights) {
		long seed = 299;
		RNG rngBalls  = new RNG(2*seed);
		
//		F1<Material, Vector> grid = v -> Material.matte(v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1);
		
		Collection<Solid> solids = new ArrayList<>();
//		Collections.addAll(solids,
////				HalfSpace.pn(Vec3.xyz(-1,  0,  0), Vec3.xyz( 1,  0,  0), grid),
////				HalfSpace.pn(Vec3.xyz( 1,  0,  0), Vec3.xyz(-1,  0,  0), grid),
////				HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), grid)
////				HalfSpace.pn(Vec3.xyz( 0,  1,  0), Vec3.xyz( 0, -1,  0), grid),
////				HalfSpace.pn(Vec3.xyz( 0,  0,  1), Vec3.xyz( 0,  0, -1), grid)
//		);
		
//		for (int i = 0; i < nBalls; i++) {
//			double hue = rngBalls.nextDouble();
//			Material material =
//					rngBalls.nextDouble() < 0.2 ?
//						Material.MIRROR :
//						Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
//			solids.add(Ball.cr(Vec3.random(rngBalls).ZOtoMP(), 0.03, uv -> material));
//		}

		for (int i = 0; i < 20; i++) {
			Vec3 pos = Vec3.random(rngBalls).ZOtoMP();
			for (int j = 0; j < 20; j++) {
				double hue = rngBalls.nextDouble();
				Material material =
						rngBalls.nextDouble() < 0.2 ?
								Material.MIRROR :
								Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
				solids.add(Ball.cr(Vec3.random(rngBalls).ZOtoMP().div(10).add(pos), 0.02, uv -> material));
			}
		}

		for (int i = 0; i < 20; i++) {
			double hue = rngBalls.nextDouble();
			Material material =
					rngBalls.nextDouble() < 0.2 ?
							Material.MIRROR :
							Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
			solids.add(Ball.cr(Vec3.random(rngBalls).ZOtoMP(), 0.02, uv -> material));
		}
		
		solid = MyGroupWithSAH.of(solids);
		
		
		RNG rngLights = new RNG(2*seed + 1);
		
		for (int i = 0; i < 2*nLights; i++) {
			lights.add(Light.pc(
					Vec3.random(rngLights).ZOtoMP(),
					Color.hsb(rngLights.nextDouble(), 0.75, 1))
			);
		}
	}
	
}
