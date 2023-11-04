package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.functions.A2;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.gui.UtilsGL;
import xyz.marsavic.resources.Resource;
import xyz.marsavic.utils.Hashing;
import xyz.marsavic.utils.Utils;


class Aggregate {
	private final A2<Matrix<Color>, Long> aFill;
	private final Vector size;
	private final long seed;
	

	private State state = null;
	
	
	
	protected record State(
			int count,
			Resource<Matrix<Color>> rSum
	) {
		private void release() {
			rSum.release();
		}
		
		// TODO: avg() should cache the result, so that it does not need to compute it again if called again. However we must not save the same resource for
		//  later because it will be released somewhere. Use reference counting somehow?
		private Resource<Matrix<Color>> avg() {
			return rSum.f(sum -> {
				Resource<Matrix<Color>> rAvg = UtilsGL.matricesColor.borrow(sum.size(), true);
				rAvg.a(avg -> Matrix.mul(sum, 1.0 / count, avg));
				return rAvg;
			});
		}
	}
	

	public Aggregate(A2<Matrix<Color>, Long> aFill, Vector size, long seed) {
		this.aFill = aFill;
		this.size = size;
		this.seed = seed;
		state = new State(0, UtilsGL.matricesColor.borrow(size));
	}
	
	
	private State getState() {
		return state;
	}
	
	
	private void setState(State stateNew) {
		State stateOld = state;
		state = stateNew;
		stateOld.release();
	}
	
	
	public synchronized void release() {
		if (state != null) {
			state.release();
		}
	}
	
	public synchronized void addSample() {
		State stateOld = getState(); // TODO Concurrency problem: stateOld can be released before we use its resource a few lines down.
		State stateNew = new State(stateOld.count + 1, UtilsGL.matricesColor.borrow(size, true));
		
		UtilsGL.matricesColor.borrow(size, mSample -> {
			// Instead of rewriting the same "sum" matrix, we borrow a new one and release the old one. This is
			// done to avoid dealing with concurrency issues. It would be faster and more memory efficient if we
			// added the new sample in-place, but it's more troublesome.
			
			aFill.execute(mSample, Hashing.mix(seed, state.count));
			
			stateNew.rSum.a(mSumNew -> {
				stateOld.rSum.a(mSumOld -> {
					Matrix.add(mSumOld, mSample, mSumNew);
				});
			});
			
			setState(stateNew);
			synchronized (hasSamplesMonitor) {
				hasSamplesMonitor.notifyAll();
			}
		});
	}
	
	
	private final Object hasSamplesMonitor = new Object();
	
	public Resource<Matrix<Color>> avg() {
		return getState().avg();
	}
	
	
	/** If there are no samples it will block while waiting for at least one sample before computing the average. */
	public Resource<Matrix<Color>> avgAtLeastOne() {
		synchronized (hasSamplesMonitor) {
			Utils.waitWhile(hasSamplesMonitor, () -> state == null || state.count == 0);
		}
		return getState().avg();
	}
	
	
}
