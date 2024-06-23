package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.gui.UtilsGL;
import xyz.marsavic.random.RNG;
import xyz.marsavic.random.fixed.noise.MapToRndNumber;
import xyz.marsavic.resources.Resource;
import xyz.marsavic.utils.Hashing;
import xyz.marsavic.utils.Utils;


class Aggregate {
	private final ColorFunction colorFunction;
	private final double t;
	private final Vector size;
	private final long seed;
	

	private State state;
	
	
	
	protected record State(
			int count,
			Resource<Matrix<Color>> rSum
	) {
		private void release() {
			if (rSum != null) {
				rSum.release();
			}
		}
		
		// TODO: Should avg() cache the result, so that it does not need to compute it again if called again? However,
		//  we must not save the same resource for later because it will be released somewhere. Use reference counting
		//  somehow?
		private Resource<Matrix<Color>> avg() {
			if (count == 0) {
				throw new IllegalStateException("No samples, can not compute average.");
			}
			if (count == 1) { // For optimization only, no other reason to consider this case separately.
				return rSum.f(sum -> {
					Resource<Matrix<Color>> rAvg = UtilsGL.matricesColor.borrow(sum.size(), true);
					rAvg.a(avg -> avg.copyFrom(sum));
					return rAvg;
				});
			} else {
				return rSum.f(sum -> {
					Resource<Matrix<Color>> rAvg = UtilsGL.matricesColor.borrow(sum.size(), true);
					rAvg.a(avg -> Matrix.mul(sum, 1.0 / count, avg));
					return rAvg;
				});
			}
		}
	}
	

	public Aggregate(ColorFunction colorFunction, double t, Vector size, long seed) {
		// TODO add a boolean parameter stating whether to immediately do the first sample.
		//  If so, then optimize by sending the first sample straight into the sum matrix
		//  (instead of making a zero matrix, computing the sample, and them adding them together).
		this.colorFunction = colorFunction;
		this.t = t;
		this.size = size;
		this.seed = seed;
		state = new State(0, null);
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
		long seedFrame = Hashing.mix(seed, state.count);
		MapToRndNumber rnd = new MapToRndNumber(seedFrame);
//		MapToRndVec3 rnd = new MapToRndVec3(seedFrame);      // This would be nicer to use, but it is slower.
		
		// Instead of rewriting the same "sum" matrix, we borrow a new one and release the old one. This is
		// done to avoid dealing with concurrency issues. It would be faster and more memory efficient if we
		// added the new sample in-place, but it's more troublesome.

		State stateOld = getState(); // TODO Concurrency problem: stateOld can be released before we use its resource a few lines down.
		State stateNew = new State(stateOld.count + 1, UtilsGL.matricesColor.borrow(size, true));
		int sizeX = size.xInt();
		stateNew.rSum.a(mSumNew -> {
			if (stateOld.count == 0) {                // For optimization only, no other reason to consider this case separately.
				UtilsGL.parallelY(size, y -> {
					RNG rng = new RNG(rnd.getLong(y));
					for (int x = 0; x < sizeX; x++) {
						Color c = colorFunction.at(t + rng.nextDouble(), Vector.xy(x + rng.nextDouble(), y + rng.nextDouble()));
						mSumNew.set(x, y, c);
					}
				});
			} else {
				stateOld.rSum.a(mSumOld -> {
					UtilsGL.parallelY(size, y -> {
						RNG rng = new RNG(rnd.getLong(y));
						for (int x = 0; x < sizeX; x++) {
							Color c = colorFunction.at(t + rng.nextDouble(), Vector.xy(x + rng.nextDouble(), y + rng.nextDouble()));
							mSumNew.set(x, y, mSumOld.get(x, y).add(c));
						}
					});
				});
			}
		});
		
		setState(stateNew);
		synchronized (hasSamplesMonitor) {
			hasSamplesMonitor.notifyAll();
		}
	}
	
	
	public Resource<Matrix<Color>> avg() {
		return getState().avg();
	}
	
	
	private final Object hasSamplesMonitor = new Object();
	
	/** If there are no samples it will block while waiting for at least one sample before computing the average. */
	public Resource<Matrix<Color>> avgAtLeastOne() {
		synchronized (hasSamplesMonitor) {
			Utils.waitWhile(hasSamplesMonitor, () -> state == null || state.count == 0);
		}
		return getState().avg();
	}
	
	
}
