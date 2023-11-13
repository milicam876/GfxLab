package xyz.marsavic.gfxlab.playground;

import xyz.marsavic.elements.HasOutput;
import xyz.marsavic.functions.A2;
import xyz.marsavic.functions.A3;
import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.graphics3d.raytracing.Raytracer;
import xyz.marsavic.gfxlab.graphics3d.raytracing.RaytracerSimple;
import xyz.marsavic.gfxlab.graphics3d.scenes.SceneTest;
import xyz.marsavic.gfxlab.gui.UtilsGL;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.random.RNG;
import xyz.marsavic.random.fixed.noise.MapToRndNumber;
import xyz.marsavic.resources.Resource;

import static xyz.marsavic.elements.ElementF.e;


public class GfxLab {

	public HasOutput<F1<Resource<Matrix<Integer>>, Integer>> sink;
	
	
	public GfxLab() {
		setup2D();
	}
	
	
	private void setup2D() {
		//                       nFrames   width     height
		var eSize = e(Vec3::new, e(1.0), e(640.0), e(640.0));
		sink =
				e(Fs::frFrameToneMapping,
						new EAggregator(
								e(Fs::aFillFrameColorRandomized,
										e(Fs::transformedColorFunction,
												e(RaytracerSimple::new,
														e(SceneTest::new)
												),
												e(TransformationsFromSize.toGeometric, eSize)
										)
								),
								eSize,
								e(0xA6A08E5C173D29FL)
						),
						e(Fs::frToneMapping,
								e(ColorTransform::asColorTransformFromMatrixColor, e(new Identity()))
						)
				);
	}
	
	
}


class Fs {
	
	public static ColorFunction transformedColorFunction(ColorFunction colorFunction, Transformation transformation) {
		return p -> colorFunction.at(transformation.at(p));
	}
	
	public static A2<Matrix<Color>, Double> aFillFrameColor(ColorFunction colorFunction) {
		return (Matrix<Color> result, Double t) -> {
			result.fill(p -> colorFunction.at(t, p));
		};
	}

	/** Prettier version. For a faster version try aFillFrameColorRandomized_Faster. */
	public static A3<Matrix<Color>, Integer, Long> aFillFrameColorRandomized(ColorFunction colorFunction) {
		return (result, t, seed) -> {
			MapToRndVec3 rnd = new MapToRndVec3(seed);
			result.fill(p -> {
				Vec3 v = Vec3.xp(t, p);
				return colorFunction.at(v.add(rnd.get(v)));
			});
		};
	}
	
	// TODO try this
	public static A3<Matrix<Color>, Integer, Long> aFillFrameColorRandomized_Faster(ColorFunction colorFunction) {
		return (result, t, seed) -> {
			MapToRndNumber rnd = new MapToRndNumber(seed);
			int sizeX = result.size().xInt();
			UtilsGL.parallelY(result.size(), y -> {
				RNG rng = new RNG(rnd.getLong(y));
				for (int x = 0; x < sizeX; x++) {
					result.set(x, y, colorFunction.at(t + rng.nextDouble(), Vector.xy(x + rng.nextDouble(), y + rng.nextDouble())));
				}
			});
		};
	}
	

	public static F1<Resource<Matrix<Integer>>, Integer> frFrameToneMapping(F1<Resource<Matrix<Color>>, Integer> frFrame, F1<Resource<Matrix<Integer>>, Resource<Matrix<Color>>> frToneMapping) {
		return iFrame -> frToneMapping.at(frFrame.at(iFrame));
	}
	
	
	// Contract: When a resource is a parameter of a "pure" function, that means it will be released (consumed) inside the function.

	public static F1<Resource<Matrix<Integer>>, Resource<Matrix<Color>>> frToneMapping(F1<ColorTransform, Matrix<Color>> f_ColorTransform_MatrixColor) {
		return input -> {
			var r = input.f(mC -> {
				ColorTransform f = f_ColorTransform_MatrixColor.at(mC);
				Resource<Matrix<Integer>> rMatI = UtilsGL.matricesInt.borrow(mC.size(), true);
				rMatI.a(mI -> mI.fill((x, y) -> f.at(mC.get(x, y)).code()));
				return rMatI;
			});
			input.release(); // CONSUMING INPUT!!!
			return r;
		};
	}

	
	public static ColorFunction blend(ColorFunction cf0, ColorFunction cf1, double t) {
		return p -> cf0.at(p).mul(1-t).add(cf1.at(p).mul(t));
	}
	
}
