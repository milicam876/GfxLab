package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunctionT;
import xyz.marsavic.utils.Numeric;


public class ColorfulHypno_Igor implements ColorFunctionT {
	
	@Override
	public Color at(double t, Vector p) {
		return Color.rgb(
				Math.max(0, Numeric.sinT(60 * t + 4 * p.length() - 4 * p.angle())),
				Math.max(0, Numeric.sinT(60 * t + 4 * p.length() - 8 * p.angle())),
				Math.max(0, Numeric.sinT(60 * t + 4 * p.length() - 16 * p.angle()))
		);
	}
	
}
