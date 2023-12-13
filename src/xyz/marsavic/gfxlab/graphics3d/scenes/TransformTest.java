package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class TransformTest extends Scene.Base {
	
	public TransformTest() {
		F1<Material, Vector> grid  = v -> Material.matte(                     v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1);
		F1<Material, Vector> gridR = v -> Material.matte(Color.hsb(0   , 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		F1<Material, Vector> gridG = v -> Material.matte(Color.hsb(0.33, 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		
		Material m = Material.GLASS;
		Solid s = Ball.cr(Vec3.ZERO, 0.5, m)
			.transformed(
					Affine.IDENTITY
							.then(Affine.scaling(Vec3.xyz(0.3, 1, 1)))
							.then(Affine.rotationAboutZ(0.2))
							.then(Affine.rotationAboutX(-0.1))
			);

		Collection<Solid> solids = new ArrayList<>();
		Collections.addAll(solids,
				HalfSpace.pn(Vec3.xyz(-1,  0,  0), Vec3.xyz( 1,  0,  0), gridG),
				HalfSpace.pn(Vec3.xyz( 1,  0,  0), Vec3.xyz(-1,  0,  0), gridR),
				HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  1,  0), Vec3.xyz( 0, -1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  0,  1), Vec3.xyz( 0,  0, -1), grid),
				
				s
		);
		
		
		solid = Group.of(solids);
		
		Collections.addAll(lights,
			Light.p(Vec3.xyz( 0.7,  0.7,  0.7)),
			Light.p(Vec3.xyz(-0.7,  0.7,  0.7)),
			Light.p(Vec3.xyz( 0.7,  0.7, -0.7)),
			Light.p(Vec3.xyz(-0.7,  0.7, -0.7))
		);
	}
	
}
