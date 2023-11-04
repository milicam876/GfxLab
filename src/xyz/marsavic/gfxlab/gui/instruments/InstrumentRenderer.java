package xyz.marsavic.gfxlab.gui.instruments;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.marsavic.elements.Element;
import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Matrix;
import xyz.marsavic.gfxlab.gui.UtilsGL;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.objectinstruments.instruments.ObjectInstrument;
import xyz.marsavic.resources.Resource;
import xyz.marsavic.resources.ResourceManagerMap;
import xyz.marsavic.time.Profiler;

import java.util.concurrent.Future;


/** Calls the renderer at each pulse if the previous renderer call has returned. The call happens in another thread,
 * not in the JavaFX Application Thread in which the update method is called. */
public class InstrumentRenderer extends ObjectInstrument<Element.Output<F1<Resource<Matrix<Integer>>, Integer>>> {
	
	private final Pane pane;
	private final ImageView imageView;
	private final CheckBox chbEnabled;
	private final Spinner<Integer> spnIFrame;
	private final Button btnCopyToClipboard;
	private final TextField txfFileName;
	private final Button btnSaveImage;
	
	
	public InstrumentRenderer(Element.Output<F1<Resource<Matrix<Integer>>, Integer>> outRenderer) {
		imageView = new ImageView();
		chbEnabled = new CheckBox();
		chbEnabled.setSelected(true);
		spnIFrame = new Spinner<>();
		btnCopyToClipboard = new Button(null, new FontIcon(Resources.Ikons.COPY));
		btnSaveImage = new Button(null, new FontIcon(Resources.Ikons.SAVE));
		txfFileName = new TextField();
		
		spnIFrame.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE));
		spnIFrame.getValueFactory().setValue(-1);
		
		btnCopyToClipboard.setOnAction(event -> UtilsGL.copyImageToClipboard(image()));
		btnSaveImage.setOnAction(event -> UtilsGL.saveImageToFileWithDialog(image()));
		
		HBox controlPanel = new HBox(
				chbEnabled,
				spnIFrame,
				btnCopyToClipboard,
				btnSaveImage,
				txfFileName
		);
		
		HBox.setHgrow(txfFileName, Priority.ALWAYS);
//		controlPanel.setPrefHeight(24);
		
		pane = new VBox(imageView, controlPanel);
		
		setObject(outRenderer);
	}
	
	@Override
	public Region node() {
		return pane;
	}
	
	
	private Future<?> future = null;

	@Override
	public synchronized void update() {
		if (!chbEnabled.isSelected()) {
			return;
		}
		if (future != null && future.isDone()) {
			future = null;
		}
		if (future == null) {
			future = UtilsGL.submitTask(this::fetch);
		}
	}

	
	private final Profiler profilerFetch = UtilsGL.profiler(this, "fetch");
	private final ResourceManagerMap<Vector, WritableImage> images = new ResourceManagerMap<>(UtilsGL::createWritableImage, null);
	private int iFrameNext = 0;
	

	private void fetch() {
		profilerFetch.enter();
		
		int iFrame = spnIFrame.getValue();
		if (iFrame == -1) {
			iFrame = iFrameNext++;
		}
		
		Resource<Matrix<Integer>> rMI = object().get().at(iFrame);
		
		rMI.a(mI -> {
			Resource<WritableImage> rImage = images.borrow(mI.size(), true);
			rImage.a(image -> UtilsGL.writeMatrixToImage(image, mI));
			Platform.runLater(() -> {
				rImage.a(imageView::setImage);
				rImage.release(); // CONSUMING!!!
			});
		});
		
		rMI.release(); // CONSUMING!!!
		
		profilerFetch.exit();
	}
	
	
	private Image image() {
		return imageView.getImage();
	}
	
}
