package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Light;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Box;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.utils.Numeric;

import java.util.*;


public class MyScene extends Scene.Base {

    public MyScene(int n) {

        Box box = Box.$.pq(Vec3.xyz(-0.5,-0.5,1.5), Vec3.xyz(0.5,0.5,2.5));
        //box = box.material(v -> Material.matte(Color.rgb(1,0,0)));

        Collection<Solid> boxes = new ArrayList<Solid>();
        boxes.add(box);

        for(int i = 0; i < n; i++){

            boxes = vratiKocke(boxes);

        }

        Ball ball = Ball.cr(Vec3.xyz(0, 0, 2), 1,
                v -> Numeric.mod(v.dot(Vector.xy(5, 4))) < 0.2 ?
                        Material.matte(Color.okhcl(v.y(), 0.125, 0.75)) :
                        Material.matte(0.1)
        );
        HalfSpace floor = HalfSpace.pn(Vec3.xyz(0, -1, 3), Vec3.xyz(0, 1, 0),
                v -> Material.matte(v.add(Vector.xy(0.05)).mod().min() < 0.1 ? 0.5 : 1)
        );

        boxes.add(floor);

        solid = Group.of(boxes);

        Collections.addAll(lights,
                Light.pc(Vec3.xyz(-1, 1, 0), Color.WHITE),
                Light.pc(Vec3.xyz(1, 1, 0), Color.WHITE),
                Light.pc(Vec3.xyz(-1, 1, 4), Color.WHITE),
                Light.pc(Vec3.xyz(1, 1, 4), Color.WHITE)
        );
    }

    private Collection<Solid> vratiKocke(Collection<Solid> boxes) {
        Collection<Solid> nove = new ArrayList<Solid>();

        for(Solid b : boxes){
            nove.addAll(izdeliKocku((Box) b));
        }

        return nove;
    }

    private Collection<Solid> izdeliKocku(Box box){
        Collection<Solid> boxes = new ArrayList<Solid>();
        Vec3 d = box.d().div(3.0);
        double x = d.x();
        double y = d.y();
        double z = d.z();
        Vec3 pos = box.p();
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                for(int k = 0; k < 3; k++){

                    if(!((i==0&&j==1&&k==1)||(i==1&&j==0&&k==1)||(i==1&&j==1&&k==0) ||
                            (i==2&&j==1&&k==1)||(i==1&&j==2&&k==1)||(i==1&&j==1&&k==2) ||
                            (i==1&&j==1&&k==1))
                    ){
                        Box b = Box.$.pd(pos.add(Vec3.xyz(i*x, j*y, k*z)), d);
                        boxes.add(b);
                    }

                }
            }
        }
        return boxes;
    }

}
