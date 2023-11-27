package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Light;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.random.RNG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class MirrorBalls extends Scene.Base {
	
	public MirrorBalls(int nBalls) {
		F1<Material, Vector> grid  = v -> Material.matte(                     v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1);
		F1<Material, Vector> gridR = v -> Material.matte(Color.hsb(0   , 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		F1<Material, Vector> gridG = v -> Material.matte(Color.hsb(0.33, 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));

		
		Collection<Solid> solids = new ArrayList<>();
		Collections.addAll(solids,
				HalfSpace.pn(Vec3.xyz(-1,  0,  0), Vec3.xyz( 1,  0,  0), gridG),
				HalfSpace.pn(Vec3.xyz( 1,  0,  0), Vec3.xyz(-1,  0,  0), gridR),
				HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  1,  0), Vec3.xyz( 0, -1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  0,  1), Vec3.xyz( 0,  0, -1), grid)
		);
		
		for (int i = 0; i < nBalls; i++) {
			Vector xy = Vector.polar(0.5, 1.0 * i / nBalls);
			solids.add(Ball.cr(Vec3.zp(0, xy), 0.4, Material.MIRROR));
		}
		
		solid = Group.of(solids);
		
		Collections.addAll(lights,
			Light.p(Vec3.xyz( 0.8,  0.8,  0.8)),
			Light.p(Vec3.xyz(-0.8,  0.8,  0.8)),
			Light.p(Vec3.xyz( 0.8,  0.8, -0.8)),
			Light.p(Vec3.xyz(-0.8,  0.8, -0.8))
		);
	}
	
}
