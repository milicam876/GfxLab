package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.elements.Invalidatable;
import xyz.marsavic.functions.A3;
import xyz.marsavic.functions.F3;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.resources.Resource;


public abstract class Aggregator extends Invalidatable.Base {
	public abstract Resource<Matrix<Color>> rFrame(int iFrame);
	
	/** This aggregator will no longer be used, release the acquired resources. */
	public abstract void release();
	
	
	
	
	public interface F_Aggregator extends F3<Aggregator, A3<Matrix<Color>, Integer, Long> , Vec3, Long> {
		@Override
		Aggregator at(A3<Matrix<Color>, Integer, Long> aFillFrameColorRandomized, Vec3 outSize, Long seed);
	}
	
}
