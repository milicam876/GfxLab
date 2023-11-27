package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.elements.Invalidatable;
import xyz.marsavic.functions.F3;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.resources.Resource;


public abstract class Aggregator extends Invalidatable.Base {
	public abstract Resource<Matrix<Color>> rFrame(int iFrame);
	
	/** Call to this method signals that this aggregator will no longer be used, and that the acquired resources can be released. */
	public abstract void release();
	
	
	
	
	public interface F_Aggregator extends F3<Aggregator, ColorFunction, Vec3, Long> {
		@Override
		Aggregator at(ColorFunction aColorFunction, Vec3 outSize, Long seed);
	}
	
}
