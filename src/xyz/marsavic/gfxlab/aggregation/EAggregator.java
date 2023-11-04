package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.elements.ElementF;
import xyz.marsavic.elements.HasOutput;
import xyz.marsavic.functions.A1;
import xyz.marsavic.functions.A3;
import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.aggregation.Aggregator;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.reactions.values.EventInvalidated;
import xyz.marsavic.resources.Resource;


public class EAggregator extends ElementF<F1<Resource<Matrix<Color>>, Integer>> {
	
	private final Input<A3<Matrix<Color>, Integer, Long>> inAFillFrameColorRandomized;
	private final Input<Vec3> inSize;
	private final Input<Long> inSeed;
	
	private final A1<EventInvalidated> onSampleAdded = this::onSampleAdded;
	
	
	
	public EAggregator(HasOutput<A3<Matrix<Color>, Integer, Long>> outAFillFrameColorRandomized, HasOutput<Vec3> outSize, HasOutput<Long> outSeed) {
		inAFillFrameColorRandomized = new Input<>(outAFillFrameColorRandomized);
		inSize = new Input<>(outSize);
		inSeed = new Input<>(outSeed);
		
		buildItUpFirstTime();
	}
	
	
	private final Aggregator.F_Aggregator factory = AggregatorFrameLast::new;
	
	private Aggregator aggregator;
	
	
	@Override
	protected void buildItUp() {
		aggregator = factory.at(inAFillFrameColorRandomized.get(), inSize.get(), inSeed.get());
		aggregator.onInvalidated().add(onSampleAdded);
	}
	
	@Override
	protected void tearItDown() {
		aggregator.release();
	}
	
	public F1<Resource<Matrix<Color>>, Integer> result() {
		return iFrame -> aggregator.rFrame(iFrame);
	}
	
	private void onSampleAdded(EventInvalidated e) {
		out.fireInvalidated();
	}
	
}
