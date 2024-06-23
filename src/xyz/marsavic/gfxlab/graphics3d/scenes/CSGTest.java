package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Box;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static xyz.marsavic.gfxlab.graphics3d.Solid.*;


public class CSGTest extends Scene.Base {
	
	public CSGTest() {
		F1<Material, Vector> grid  = v -> Material.matte(                     v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1);
		F1<Material, Vector> gridR = v -> Material.matte(Color.hsb(0   , 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		F1<Material, Vector> gridG = v -> Material.matte(Color.hsb(0.33, 0.5, v.add(Vector.xy(0.005)).mod(0.25).min() < 0.01 ? 0.5 : 1));
		
		
		Material m = Material.glass(0.9);
		
		Solid sA = Box.$.r(0.5).material(m).transformed(Affine.rotationAboutX(0.1).then(Affine.rotationAboutY(0.1)));
		Solid sB = Ball.cr(Vec3.xyz(0, 0, 0), 0.62, m);
		Solid sC = Ball.cr(Vec3.xyz(0, 0, 0), 0.68, m);
		Solid s = Solid.intersection(Solid.difference(sA, sB), sC).withMaterial(Material.matte(Color.hsb(0.2, 0.8, 1.0)));
		
		Collection<Solid> solids = new ArrayList<>();
		Collections.addAll(solids,
				HalfSpace.pn(Vec3.xyz(-1,  0,  0), Vec3.xyz( 1,  0,  0), gridG),
				HalfSpace.pn(Vec3.xyz( 1,  0,  0), Vec3.xyz(-1,  0,  0), gridR),
				HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  1,  0), Vec3.xyz( 0, -1,  0), grid),
				HalfSpace.pn(Vec3.xyz( 0,  0,  1), Vec3.xyz( 0,  0, -1), grid),
				
				s
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
