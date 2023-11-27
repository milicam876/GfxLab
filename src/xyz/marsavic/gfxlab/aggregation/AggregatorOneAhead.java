package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.gui.UtilsGL;
import xyz.marsavic.resources.Resource;
import xyz.marsavic.utils.Hashing;

import java.util.concurrent.Future;


public class AggregatorOneAhead extends Aggregator {
	
	private final ColorFunction colorFunction;
	private final int nFrames;
	private final Vector sizeFrame;
	private final long seed;
	
	
	private Future<Resource<Matrix<Color>>> frFrameAhead;
	private int iFrameAhead_;
	
	
	
	public AggregatorOneAhead(ColorFunction colorFunction, Vec3 size, long seed) {
		this.colorFunction = colorFunction;
		nFrames = (int) size.x();
		sizeFrame = size.p12();
		this.seed = seed;
	
		iFrameAhead_ = 0;
		startGettingAhead(iFrameAhead_);
	}
	
	
	private void startGettingAhead(int iFrame) {
		frFrameAhead = UtilsGL.submitTask(() -> rFrameCompute(iFrame));
	}
	
	
	private Resource<Matrix<Color>> rFrameCompute(int iFrame_) {
		Aggregate aggregate = new Aggregate(colorFunction, iFrame_, sizeFrame, Hashing.mix(seed, iFrame_));
		aggregate.addSample();
		var avg = aggregate.avg();
		aggregate.release();
		return avg;
	}
	
	
	@Override
	public Resource<Matrix<Color>> rFrame(int iFrame) {  // not thread safe
		int iFrame_ = iFrame % nFrames;
		Resource<Matrix<Color>> rFrame;
		
		if (iFrameAhead_ == iFrame_) {
			rFrame = UtilsGL.futureGet(frFrameAhead);
		} else {
			releaseRFrameAhead();
			rFrame = rFrameCompute(iFrame_);
		}
		iFrameAhead_ = (iFrame_ + 1) % nFrames;
		startGettingAhead(iFrameAhead_);
		return rFrame;
	}
	
	
	private void releaseRFrameAhead() {
		var frFrame_ = frFrameAhead;
		UtilsGL.submitTask(() -> UtilsGL.futureGet(frFrame_).release());
	}
	
	
	@Override
	public void release() {
		releaseRFrameAhead();
	}
}
