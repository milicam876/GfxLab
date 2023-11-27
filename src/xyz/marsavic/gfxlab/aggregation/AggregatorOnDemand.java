package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.resources.Resource;
import xyz.marsavic.utils.Hashing;


public class AggregatorOnDemand extends Aggregator {
	
	private final ColorFunction colorFunction;
	private final int nFrames;
	private final Vector sizeFrame;
	private final long seed;
	
	private Aggregate aggregate;
	
	
	
	public AggregatorOnDemand(ColorFunction colorFunction, Vec3 size, long seed) {
		this.colorFunction = colorFunction;
		nFrames = (int) size.x();
		sizeFrame = size.p12();
		this.seed = seed;
	}
	
	
	@Override
	public Resource<Matrix<Color>> rFrame(int iFrame) {
		if (aggregate != null) {
			aggregate.release();
		}
		
		int iFrame_ = iFrame % nFrames;
		
		aggregate = new Aggregate(colorFunction, iFrame_, sizeFrame, Hashing.mix(seed, iFrame));
		aggregate.addSample();
		var avg = aggregate.avg();
		aggregate.release();
		return avg;
		
	}
	
	
	@Override
	public void release() {
		if (aggregate != null) {
			aggregate.release();
		}
	}
}
