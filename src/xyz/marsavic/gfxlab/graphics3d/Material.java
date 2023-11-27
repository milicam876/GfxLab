package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;


public record Material (
		Color diffuse,
		Color specular,
		double shininess,
		Color reflective
) implements F1<Material, Vector> {
	public Material diffuse        (Color  diffuse   ) { return new Material(diffuse, specular, shininess, reflective); }
	public Material specular       (Color  specular  ) { return new Material(diffuse, specular, shininess, reflective); }
	public Material shininess      (double shininess ) { return new Material(diffuse, specular, shininess, reflective); }
	public Material reflective     (Color  reflective) { return new Material(diffuse, specular, shininess, reflective);}

	
	public static final Material BLACK   = new Material(Color.BLACK, Color.BLACK, 1, Color.BLACK);
	
	public static Material matte (Color  c) { return BLACK.diffuse(c); }
	public static Material matte (double k) { return matte(Color.gray(k)); }
	public static Material matte (        ) { return matte(1.0); }
	public static final Material MATTE = matte();
	
	
	public static final Material DEFAULT = MATTE;
	
	public static final Material MIRROR = BLACK.reflective(Color.WHITE);
	
	
	@Override
	public Material at(Vector vector) {
		return this;
	}
}
