package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.functions.A0;
import xyz.marsavic.functions.A3;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.gui.UtilsGL;
import xyz.marsavic.resources.Resource;
import xyz.marsavic.time.Profiler;
import xyz.marsavic.utils.Hashing;
import xyz.marsavic.utils.OnGC;
import xyz.marsavic.utils.Utils;

import java.util.HashMap;
import java.util.Map;


public class AggregatorFrame0 extends Aggregator {
	
	private final int nFrames;
	private final Aggregate[] aggregates;
	private final Profiler profilerRemoveMePls = UtilsGL.profiler(this, "loop");
	private final A0 aStopLoop;
	
	
	
	
	public AggregatorFrame0(A3<Matrix<Color>, Integer, Long> aFillFrameColorRandomized, Vec3 size, long seed) {
		nFrames = (int) size.x();
		Vector sizeFrame = size.p12();
		
		aggregates = new Aggregate[nFrames];
		
		for (int iFrame = 0; iFrame < 1; iFrame++) {
			int i = iFrame;
			aggregates[i]= new Aggregate(
					(Matrix<Color> m, Long seedFrame) -> aFillFrameColorRandomized.execute(m, i, seedFrame),
					sizeFrame, Hashing.mix(seed, iFrame)
			);
		}
		
		aStopLoop = Utils.daemonLoop(() -> {
			// TODO FIXME !!!!!!!!!!!! NOOOOOOOOO!!!!!!! This is just a temporary test.
			profilerRemoveMePls.enter();
			addSample(0);
			profilerRemoveMePls.exit();
		});
		OnGC.setOnGC(this, aStopLoop);
	}
	
	
	private Aggregate getAggregate(int iFrame) {
		return aggregates[0]; // TODO
	}
	
	
	@Override
	public Resource<Matrix<Color>> rFrame(int iFrame) {
		iFrame %= nFrames;
		return getAggregate(iFrame).avg();
	}
	
	
	private void addSample(int iFrame) {
		getAggregate(iFrame).addSample();
		fireInvalidated(); // TODO Fire EventInvalidatedFrame instead
	}
	
	
	@Override
	public void release() {
		aStopLoop.execute();
		for (Aggregate aggregate : aggregates) {
			aggregate.release();
		}
	}
	
	
	// ------------------------------------------------------------------------------------------------------
	
	
}