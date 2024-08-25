package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.DoubleUnaryOperator;

public class MyGroupWithCache extends Solid {

    private static final int THRESHOLD_NUM = 1;
    private double THRESHOLD_SIZE = 0.01;

    private Node root;

    protected class Node {

        /**
         * svaki box koji ima vise od treshold broja objekata i stranica mu je veca od treshold duzine
         * delimo na 8 jednakih boxova
         *
         * prvi nivo po x, drugi po y, treci po z
         * uvek manje vrednosti levo, vece desno
         *
         * za svaki novi box pamtimo novu sirinu, duplo manju po odg osi, i novi centar odgovarajuci
         *
         * na svakom kockastom nivou, proverimo tresholde, i rekurzivno delimo, kad vise ne delimo, proglasimo to listom
         *
         * svaki solid vraca svoj bounding box
         */

        protected BoundingBox boundingBox;

        protected Solid[] solids; // solidi koji seku dati cvor
        protected Node left, right; // levo i desno dete
        protected Axis axis; // X (pocetni bb - kocka), Y, Z
        protected double splittingPlane; // mesto slPl na odgovarajucoj osi (axis)

        public Node(BoundingBox boundingBox, Solid[] solids, Axis axis){
            //konstruktor - uzima prosldjene vrednosti i poziva rekurzivno pravljenje dece
            this.boundingBox = boundingBox;
            this.solids = solids;
            this.axis = axis;
            if (axis != Axis.X || (solids.length > THRESHOLD_NUM && boundingBox.r().lengthSquared() > THRESHOLD_SIZE)){
                split(); //napravi spliting plane i decu
            }
        }

        private void split(){
            splittingPlane = boundingBox.c().get(this.axis.index());
            createNode(-1);//left
            createNode(1);//right
        }

        private void createNode(int mul){
            Axis axis = this.axis.next();
            int a = this.axis.index();
            Vec3 c = Vec3.f(DoubleUnaryOperator.identity(), this.boundingBox.c());
            Vec3 r = Vec3.f(DoubleUnaryOperator.identity(), this.boundingBox.r());

            r = Vec3.set(a, r.get(a)/2, r);
            c = Vec3.set(a, c.get(a)+mul*r.get(a), c);

            BoundingBox bb = BoundingBox.$.cr(c, r);
            Solid[] solids;

            //if (axis!=Axis.X) solids = Arrays.copyOf(this.solids, this.solids.length);
            solids = filterSolids(this.solids, bb);
            //System.out.println(bb + " " + solids.length);

            if (mul == -1){ left = new Node(bb, solids, axis); }
            else { right = new Node(bb, solids, axis); }
        }


        private Solid[] filterSolids(Solid[] solids, BoundingBox boundingBox) {
            ArrayList<Solid> l = new ArrayList<Solid>();
            for(Solid solid : solids){
                if (solid.intersects(boundingBox)) {
                    l.add(solid);
                }
            }

            return l.toArray(new Solid[l.size()]);
        }

        public boolean isLeaf(){
            return left == null;
        }

    }

    private MyGroupWithCache(Solid[] solids) {
        this.boundingBox = boundingBox(solids);
        this.THRESHOLD_SIZE = boundingBox.r().lengthSquared()/16;
        this.root = new Node(boundingBox, solids, Axis.X);
        //System.out.println(root.boundingBox);
        //ispisi(root);
    }

    private void ispisi(Node root) {
        if (root == null) return;
        System.out.println(root.boundingBox);
        ispisi(root.left);
        ispisi(root.right);
    }

    private BoundingBox boundingBox(Solid[] solids) {
        //kreira veliki bounding box svih solida

        if (solids.length == 0) return null;

        Vec3 min = solids[0].boundingBox().p();
        Vec3 max = solids[0].boundingBox().q();

        for(Solid solid : solids){
            Vec3 p = solid.boundingBox().p();
            Vec3 q = solid.boundingBox().q();

            min = Vec3.min(min, p);
            max = Vec3.max(max, q);
        }

        return BoundingBox.$.pq(min, max);
    }

    public static MyGroupWithCache of(Solid... solids) {
        return new MyGroupWithCache(solids);
    }

    public static MyGroupWithCache of(Collection<Solid> solids) {
        return new MyGroupWithCache(solids.toArray(Solid[]::new));
    }

    /**
     * nadjemo entry i exit pozicije za pocetni bb -> hits s bb
     * ako nema hita -- vratimo hit u beskonacnosti
     * ako ima, klasifikujemo decu trenutnog cvora na near i far
     * u zavisnosti od slucaja, obilazimo samo near, samo far ili near pa far rekurzivno
     * kad stignemo do lista, proverimo je l ima hita medju njegovim solidima
     *
     * napomena: moze da se desi da nadjemo hit s solidom van trenutnog noda
     * u tom slucaju ne vracamo odmah, nego nastavljamo pretragu, jer mozda ima neki blizi
     *
     * u nekom momentu mozda implementirati neki cache za ovo, da se ne racuna hit za isti objekat vise puta
     */

    @Override
    public Hit firstHit(Ray ray, double afterTime) {

        double entryTime = this.boundingBox.firstHit(ray, afterTime);
        if (Double.isNaN(entryTime)) return Hit.AtInfinity.axisAlignedGoingIn(ray.d());

        double exitTime = this.boundingBox.firstHit(ray, entryTime);
        if (Double.isNaN(exitTime)) {
            exitTime = entryTime;
            entryTime = 0;
        }

        HashMap<Solid, Hit> cache = new HashMap<Solid, Hit>();

        return findHit(root, ray, afterTime, entryTime, exitTime, cache);
    }

    private Hit findHit(Node node, Ray ray, double afterTime, double entryTime, double exitTime, HashMap<Solid, Hit> cache){

        //ako je leaf, provera hitova
        if(node.isLeaf()){
            Hit minHit = Nothing.INSTANCE.firstHit(ray, afterTime);
            double minT = minHit.t();

            for (Solid solid : node.solids) {
                Hit hit;
                if (cache.containsKey(solid)) hit = cache.get(solid);
                else {
                    hit = solid.firstHit(ray, afterTime);
                    cache.put(solid, hit);
                }

                double t = hit.t();
                if (t >= entryTime && t <= exitTime && t < minT) {
                    minT = t;
                    minHit = hit;
                }
            }

            return minHit;
        }

//        Hit hit1 = findHit(node.left, ray, afterTime, entryTime, exitTime);
//        Hit hit2 = findHit(node.right, ray, afterTime, entryTime, exitTime);
//
//        return hit1.t() < hit2.t() ? hit1 : hit2;

        // klasifikuj near i far
        int a = node.axis.index();
        Node near, far;

        if(node.splittingPlane > ray.p().get(a)) { near = node.left; far  = node.right; }
        else                                     { near = node.right; far  = node.left; }

        // u zavisnosti od slucaja ispitaj samo near, samo far ili near pa far rekurzivno
        double t = (node.splittingPlane - ray.p().get(a)) / ray.d().get(a);

        if(t > exitTime || t < 0) { return findHit(near, ray, afterTime, entryTime, exitTime, cache); }
        else if(t <= entryTime)   { return findHit(far , ray, afterTime, entryTime, exitTime, cache); }
        else{
            Hit hit = findHit(near, ray, afterTime, entryTime, t, cache);
            if(hit.getClass() != Hit.AtInfinity.class) return hit; //nzm je l ovo moze ovako da se proveri
            return findHit(far, ray, afterTime, t, exitTime, cache);
        }
    }

    //@Override
    public boolean intersects(BoundingBox boundingBox) {
        return true;
    }

    @Override
    public boolean hitBetween(Ray ray, double afterTime, double beforeTime) {
        return this.firstHit(ray, afterTime).t() < beforeTime;
    }

}
