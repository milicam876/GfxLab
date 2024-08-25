package xyz.marsavic.gfxlab.Test;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.graphics3d.cameras.Perspective;
import xyz.marsavic.gfxlab.graphics3d.cameras.TransformedCamera;
import xyz.marsavic.gfxlab.graphics3d.solids.*;
import xyz.marsavic.random.RNG;

import java.util.ArrayList;
import java.util.Collection;

public class TestSAH {
    public static void main(String[] args) {

        long seed = 299;
        RNG rng  = new RNG(2*seed);

        Collection<Solid> solids = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Vec3 pos = Vec3.random(rng).ZOtoMP();
            for (int j = 0; j < 20; j++) {
                double hue = rng.nextDouble();
                Material material =
                        rng.nextDouble() < 0.2 ?
                                Material.MIRROR :
                                Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
                solids.add(Ball.cr(Vec3.random(rng).ZOtoMP().div(10).add(pos), 0.02, uv -> material));
            }
        }

        for (int i = 0; i < 20; i++) {
            double hue = rng.nextDouble();
            Material material =
                    rng.nextDouble() < 0.2 ?
                            Material.MIRROR :
                            Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
            solids.add(Ball.cr(Vec3.random(rng).ZOtoMP(), 0.02, uv -> material));
        }
//
//        for (int i = 0; i < 200; i++) {
//            double hue = rng.nextDouble();
//            Material material =
//                    rng.nextDouble() < 0.2 ?
//                            Material.MIRROR :
//                            Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
//            solids.add(Ball.cr(Vec3.random(rng).div(2).add(Vec3.c(0.5)), 0.02, uv -> material));
//        }
//
//        for (int i = 0; i < 100; i++) {
//            double hue = rng.nextDouble();
//            Material material =
//                    rng.nextDouble() < 0.2 ?
//                            Material.MIRROR :
//                            Material.matte(Color.hsb(hue, 0.9, 0.9)).specular(Color.WHITE).shininess(32);
//            solids.add(Ball.cr(Vec3.random(rng).div(2).add(Vec3.c(-1)), 0.02, uv -> material));
//        }

        Solid group = Group.of(solids);
        Solid myGroup = MyGroupWithCache.of(solids);
        Solid groupNoCache = MyGroup.of(solids);
        Solid groupSAH = MyGroupWithSAH.of(solids);

        Camera camera = new TransformedCamera(new Perspective(1.0 / 6),
                Affine.IDENTITY.then(Affine.translation(Vec3.xyz(0,0,-3))));

        long time = System.nanoTime();

//        for (int i = 0; i < 10000000; i++){
//            Ray ray = camera.exitingRay(Vector.xy(rng.nextDouble(), rng.nextDouble()).mul(2).sub(Vector.xy(1,1)).mul(0.3));
//            Hit hit = group.firstHit(ray);
////            if(hit.t() < Double.POSITIVE_INFINITY) t += hit.t();
////            if(t > 10000) t-= 10000;
//        }
//
//        //System.out.println(t);
//        System.out.println("Linearno:" + (System.nanoTime()-time));

//        time = System.nanoTime();
//
//        for (int i = 0; i < 10000000; i++){
//            Ray ray = camera.exitingRay(Vector.xy(rng.nextDouble(), rng.nextDouble()).mul(2).sub(Vector.xy(1,1)).mul(0.3));
//            Hit hit = myGroup.firstHit(ray);
////            if(hit.t() < Double.POSITIVE_INFINITY) t += hit.t();
////            if(t > 10000) t-= 10000;
//        }
//
//        //System.out.println(t);
//        System.out.println("Cache:   " + (System.nanoTime()-time));

        time = System.nanoTime();

        for (int i = 0; i < 10000000; i++){
            Ray ray = camera.exitingRay(Vector.xy(rng.nextDouble(), rng.nextDouble()).mul(2).sub(Vector.xy(1,1)).mul(0.3));
            Hit hit = groupNoCache.firstHit(ray);
//            if(hit.t() < Double.POSITIVE_INFINITY) t += hit.t();
//            if(t > 10000) t-= 10000;
        }

        System.out.println("NOCache: " + (System.nanoTime()-time));

        time = System.nanoTime();

        for (int i = 0; i < 10000000; i++){
            Ray ray = camera.exitingRay(Vector.xy(rng.nextDouble(), rng.nextDouble()).mul(2).sub(Vector.xy(1,1)).mul(0.3));
            Hit hit = groupSAH.firstHit(ray);
//            if(hit.t() < Double.POSITIVE_INFINITY) t += hit.t();
//            if(t > 10000) t-= 10000;
        }

        System.out.println("SAH:     " + (System.nanoTime()-time));

    }
}
