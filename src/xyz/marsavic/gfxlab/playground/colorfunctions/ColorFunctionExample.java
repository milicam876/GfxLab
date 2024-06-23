package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunctionT;
import xyz.marsavic.elements.Element;

public class ColorFunctionExample implements ColorFunctionT {
	
	@Override
	public Color at(double t, Vector p) {
		return Color.rgb(p.x(), 1 - p.x(), 0 );
	}
	
}
