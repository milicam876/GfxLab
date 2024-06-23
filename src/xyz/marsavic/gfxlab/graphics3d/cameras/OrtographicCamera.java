package xyz.marsavic.gfxlab.graphics3d.cameras;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Camera;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.utils.Numeric;

public record OrtographicCamera() implements Camera {

    @Override
    public Ray exitingRay(Vector p) {
        return Ray.pd(Vec3.zp(0, p), Vec3.xyz(0,0,1));
    }

}
