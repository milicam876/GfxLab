package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.utils.Numeric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class Oranges extends Scene.Base {
	
	public Oranges() {

		int n = 4;
		double sqrt3 = Math.sqrt(3);
		
		Vec3 dI = Vec3.xyz(1.0, 0.0, 0.0);
		Vec3 dJ = Vec3.xyz(0.5, 0.0, sqrt3/2);
		Vec3 dK = Vec3.xyz(0.5, Math.sqrt(2.0/3.0), 1.0/(2*sqrt3));
		
		Vec3 o = dI.add(dJ.add(dK)).mul((n-1)/4.0);
		
		double d = 1.6 / n;
		
		Collection<Solid> solids = new ArrayList<>();
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; i+j < n; j++) {
				for (int k = 0; i+j+k < n; k++) {
					Vec3 c = dI.mul(i).add(dJ.mul(j)).add(dK.mul(k)).sub(o).mul(d).sub(Vec3.EY.mul(1-d/2-d*o.y()));
					solids.add(Ball.cr(c, d/2,
							v -> Numeric.mod(v.dot(Vector.xy(3, 2))) < 0.2 ? Material.light(Color.okhcl(v.y(), 0.12, 0.75)) : Material.matte(0.7)
					));
				}
			}
		}
		
		Collections.addAll(solids,
				HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), v -> Material.matte(0.7))
		);
		
		solid = Group.of(solids);
	}
	
}
