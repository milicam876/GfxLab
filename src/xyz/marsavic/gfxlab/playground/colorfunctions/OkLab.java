package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunctionT;


public class OkLab implements ColorFunctionT {
	
	@Override
	public Color at(double t, Vector p) {
		return Color.okhcl(t, p.x(), p.y());
	}
	
}
