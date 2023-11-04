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
import xyz.marsavic.utils.performance.ArrayIntsSynchronized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AggregatorFrameLast extends Aggregator {
	
	private final A3<Matrix<Color>, Integer, Long> aFillFrameColorRandomized;
	private final int nFrames;
	private final Vector sizeFrame;
	private final long seed;
	
	
	private int iFrameLast = -1;
	private Aggregate aggregate;
	private final Profiler profilerRemoveMePls = UtilsGL.profiler(this, "loop");
	private final A0 aStopLoop;
	
	
//	private final List<Aggregate> forRelease = Collections.synchronizedList(new ArrayList<>());
	
	
	public AggregatorFrameLast(A3<Matrix<Color>, Integer, Long> aFillFrameColorRandomized, Vec3 size, long seed) {
		this.aFillFrameColorRandomized = aFillFrameColorRandomized;
		nFrames = (int) size.x();
		sizeFrame = size.p12();
		this.seed = seed;
		
		changeAggregate(0);
		
		aStopLoop = Utils.daemonLoop(() -> {
			// TODO FIXME !!!!!!!!!!!! NOOOOOOOOO!!!!!!! This is just a temporary test.
			profilerRemoveMePls.enter();
			addSample();
			profilerRemoveMePls.exit();
/*
			synchronized (forRelease) {
				forRelease.forEach(Aggregate::release);
				forRelease.clear();
			}
*/
		});
		OnGC.setOnGC(this, aStopLoop);
	}
	
	
	private void changeAggregate(int iFrame) {
		Aggregate aggregateOld = aggregate;
		iFrameLast = iFrame;
		
		aggregate = new Aggregate(
				(Matrix<Color> m, Long seedFrame) -> aFillFrameColorRandomized.execute(m, iFrame, seedFrame),
				sizeFrame, Hashing.mix(seed, iFrame)
		);
		
		if (aggregateOld != null) {
//			forRelease.add(aggregateOld);
			aggregateOld.release();
		}
	}
	
	
	@Override
	public Resource<Matrix<Color>> rFrame(int iFrame) {
		iFrame %= nFrames;
		if (iFrame != iFrameLast) {
			changeAggregate(iFrame);
		}
		return aggregate.avgAtLeastOne();
	}
	
	
	private void addSample() {
		aggregate.addSample();
		fireInvalidated(); // TODO Fire EventInvalidatedFrame instead
	}
	
	
	
	@Override
	public void release() {
//		forRelease.add(aggregate);
		aStopLoop.execute();
		aggregate.release();
	}
}
