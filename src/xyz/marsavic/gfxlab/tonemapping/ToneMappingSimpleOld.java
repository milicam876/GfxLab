package xyz.marsavic.gfxlab.tonemapping;

import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Matrix;


public class ToneMappingSimpleOld {
	
	public static ToneMapping f(F1<ColorTransform, Matrix<Color>> f_ColorTransform_MatrixColor) {
		return (output, input) -> {
			ColorTransform f = f_ColorTransform_MatrixColor.at(input);
			output.fill(p -> f.at(input.get(p)).codeClamp());
/*
			// Faster but less nice version:
			int w = output.size().xInt();
			UtilsGL.parallelY(input.size(), y -> {
				for (int x = 0; x < w; x++) {
					output.set(x, y, f.at(input.get(x, y)).codeClamp());
				}
			});
*/
		};
	}
	
}
