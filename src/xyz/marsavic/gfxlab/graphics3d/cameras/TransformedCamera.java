package xyz.marsavic.gfxlab.graphics3d.cameras;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Transformation;
import xyz.marsavic.gfxlab.graphics3d.Camera;
import xyz.marsavic.gfxlab.graphics3d.Ray;


public record TransformedCamera(
		Camera camera,
		Transformation transformation
) implements Camera {
	
	@Override
	public Ray exitingRay(Vector p) {
		return transformation.at(camera.exitingRay(p));
	}
	
}
