package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.BoundingBox;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleUnaryOperator;

/*TODO:
    - intersection metod za Halfspace? -- msm da cemo samo da zabranimo beskonacne solide u grupi
 */

public class MyGroup extends Solid {

    private static final int TRESHOLD_NUM = 1;
    private double TRESHOLD_SIZE = 0.1;

    private Node root;


    public enum Axis {
        X, Y, Z;

        private static final Axis[] vals = values();

        public int index() { return this.ordinal(); }

        public Axis next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    protected class Node {

        /**
         * svaki box koji ima vise od treshold broja objekata i stranica mu je veca od treshold duzine
         * delimo na 8 jednakih boxova
         *
         * prvi nivo po x, drugi po y, treci po z
         * uvek manje vrednosti levo, vece desno
         *
         * na trecem nivou imamo kocke, tu pamtimo novu sirinu, duplo manju, i novi centar, odgovarajuci
         * na prva dva nivoa cuvamo postojeci centar i sirinu
         *
         * na svakom kockastom nivou, proverimo tresholde, i rekurzivno delimo, kad vise ne delimo, proglasimo to listom
         *
         * svaki solid vraca svoj bounding box
         * TODO: - ovo kasnije, moze i samo veliki bb da se prosledi konstruktoru
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
            if (axis != Axis.X || (solids.length > TRESHOLD_NUM && boundingBox.r().lengthSquared() > TRESHOLD_SIZE)){
                split(); //napravi spliting plane i decu
            }
        }

        private void split(){
            splittingPlane = boundingBox.c().get(this.axis.index());
            createLeft();
            createRight();
        }

        private void createLeft(){
            Axis axis = this.axis.next();
            Vec3 c = Vec3.f(DoubleUnaryOperator.identity(), this.boundingBox.c());
            Vec3 r = Vec3.f(DoubleUnaryOperator.identity(), this.boundingBox.r());

            switch (this.axis){
                case X -> {
                    r = Vec3.xr(r.x()/2, r);
                    c = Vec3.xr(this.boundingBox.c().x()-r.x(), c);
                }
                case Y -> {
                    r = Vec3.yr(r.y()/2, r);
                    c = Vec3.yr(this.boundingBox.c().y()-r.y(), c);
                }
                case Z -> {
                    r = Vec3.zr(r.z()/2, r);
                    c = Vec3.zr(this.boundingBox.c().z()-r.z(), c);
                }
            }

            BoundingBox bb = BoundingBox.$.cr(c, r);
            Solid[] solids;

            //if (axis!=Axis.X) solids = Arrays.copyOf(this.solids, this.solids.length);
            solids = filterSolids(this.solids, bb);
            //System.out.println(bb + " " + solids.length);

            left = new Node(bb, solids, axis);
        }

        private void createRight(){
            Axis axis = this.axis.next();
            Vec3 c = Vec3.f(DoubleUnaryOperator.identity(), this.boundingBox.c());
            Vec3 r = Vec3.f(DoubleUnaryOperator.identity(), this.boundingBox.r());

            switch (this.axis){
                case X -> {
                    r = Vec3.xr(r.x()/2, r);
                    c = Vec3.xr(this.boundingBox.c().x()+r.x(), c);
                }
                case Y -> {
                    r = Vec3.yr(r.y()/2, r);
                    c = Vec3.yr(this.boundingBox.c().y()+r.y(), c);
                }
                case Z -> {
                    r = Vec3.zr(r.z()/2, r);
                    c = Vec3.zr(this.boundingBox.c().z()+r.z(), c);
                }
            }

            BoundingBox bb = BoundingBox.$.cr(c, r);
            Solid[] solids;

            //if (axis!=Axis.X) solids = Arrays.copyOf(this.solids, this.solids.length);
            solids = filterSolids(this.solids, bb);

            //System.out.println(axis + " " + solids.length);

            right = new Node(bb, solids, axis);
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

    private MyGroup(Solid[] solids) {
        this.boundingBox = boundingBox(solids);
        this.TRESHOLD_SIZE = boundingBox.r().lengthSquared()/16;
        this.root = new Node(boundingBox, solids, Axis.X);
        //System.out.println(root.boundingBox);
        ispisi(root);
    }

    private void ispisi(Node root) {
        if (root == null) return;
        System.out.println(root.boundingBox);
        ispisi(root.left);
        ispisi(root.right);
    }

    private BoundingBox boundingBox(Solid[] solids) {
        //kreira veliki bounding box svih solida

        Double minx = Double.POSITIVE_INFINITY;
        Double miny = Double.POSITIVE_INFINITY;
        Double minz = Double.POSITIVE_INFINITY;

        Double maxx = Double.NEGATIVE_INFINITY;
        Double maxy = Double.NEGATIVE_INFINITY;
        Double maxz = Double.NEGATIVE_INFINITY;

        for(Solid solid : solids){
            Vec3 p = solid.boundingBox().p();
            Vec3 q = solid.boundingBox().q();

            if(p.x() < minx) minx = p.x();
            if(p.y() < miny) miny = p.y();
            if(p.z() < minz) minz = p.z();

            if(q.x() > maxx) maxx = q.x();
            if(q.y() > maxy) maxy = q.y();
            if(q.z() > maxz) maxz = q.z();
        }

        return BoundingBox.$.pd(Vec3.xyz(minx, miny, minz), Vec3.xyz(maxx, maxy, maxz));
    }

    public static MyGroup of(Solid... solids) {
        return new MyGroup(solids);
    }

    public static MyGroup of(Collection<Solid> solids) {
        return new MyGroup(solids.toArray(Solid[]::new));
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

        return findHit(root, ray, afterTime, entryTime, exitTime);
    }

    private Hit findHit(Node node, Ray ray, double afterTime, double entryTime, double exitTime){

        //ako je leaf, provera hitova
        if(node.isLeaf()){
            Hit minHit = Nothing.INSTANCE.firstHit(ray, afterTime);
            double minT = minHit.t();

            for (Solid solid : node.solids) {
                Hit hit = solid.firstHit(ray, afterTime);
                double t = hit.t();
                //TODO: mozda napraviti neki cache nekad
                if (t >= entryTime && t <= exitTime && t < minT) {
                    minT = t;
                    minHit = hit;
                }
            }

            return minHit;
        }

        // klasifikuj near i far
        int a = node.axis.index();
        Node near, far;

        if(node.splittingPlane > ray.p().get(a)) { near = node.left; far  = node.right; }
        else                                     { near = node.right; far  = node.left; }

        // u zavisnosti od slucaja ispitaj samo near, samo far ili near pa far rekurzivno
        double t = (node.splittingPlane - ray.p().get(a)) / ray.d().get(a);

        if(t > exitTime || t < 0) { return findHit(near, ray, afterTime, entryTime, exitTime); }
        else if(t <= entryTime)   { return findHit(far , ray, afterTime, entryTime, exitTime); }
        else{
            Hit hit = findHit(near, ray, afterTime, entryTime, t);
            if(hit.getClass() != Hit.AtInfinity.class) return hit; //nzm je l ovo moze ovako da se proveri
            return findHit(far, ray, afterTime, t, exitTime);
        }
    }

    //@Override
    public boolean intersects(BoundingBox boundingBox) {
        return true;
    }

    @Override
    public boolean hitBetween(Ray ray, double afterTime, double beforeTime) {
        //TODO: mozda menjati nesto?
        return this.firstHit(ray, afterTime).t() < beforeTime;
    }

}
