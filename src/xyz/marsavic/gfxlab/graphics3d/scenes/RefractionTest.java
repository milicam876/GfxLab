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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class RefractionTest extends Scene.Base {
	
	public RefractionTest() {
		F1<Material, Vector> grid  = v -> Material.matte(                     v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1);
		F1<Material, Vector> gridR = v -> Material.matte(Color.hsb(0   , 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		F1<Material, Vector> gridG = v -> Material.matte(Color.hsb(0.33, 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		
		Collection<Solid> solids = new ArrayList<>();
		Collections.addAll(solids,
				HalfSpace.pn(Vec3.xyz(-1,  0,  0), Vec3.xyz( 1,  0,  0), gridG),
				HalfSpace.pn(Vec3.xyz( 1,  0,  0), Vec3.xyz(-1,  0,  0), gridR),
				HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  1,  0), Vec3.xyz( 0, -1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  0,  1), Vec3.xyz( 0,  0, -1), grid),

				Ball.cr(Vec3.xyz(-0.3,  0.3,  0.0), 0.4, uv -> Material.GLASS.refractive(Color.hsb(0.7, 0.2, 1.0))),
				Ball.cr(Vec3.xyz( 0.4, -0.4,  0.0), 0.4, uv -> Material.GLASS),
				Ball.cr(Vec3.xyz(-0.3, -0.4, -0.6), 0.4, uv -> Material.GLASS.refractiveIndex(2.5)),
				Ball.cr(Vec3.xyz( 0.4,  0.3,  0.6), 0.4, uv -> Material.GLASS.refractiveIndex(0.6))
		);
		
		Collections.addAll(lights,
				Light.pc(Vec3.xyz(-0.7, 0.7, -0.7), Color.WHITE),
				Light.pc(Vec3.xyz(-0.7, 0.7,  0.7), Color.WHITE),
				Light.pc(Vec3.xyz( 0.7, 0.7, -0.7), Color.WHITE),
				Light.pc(Vec3.xyz( 0.7, 0.7,  0.7), Color.WHITE)
		);
		
		solid = Group.of(solids);
	}
	
}
