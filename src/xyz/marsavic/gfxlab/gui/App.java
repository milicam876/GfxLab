package xyz.marsavic.gfxlab.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import xyz.marsavic.functions.A0;
import xyz.marsavic.gfxlab.gui.instruments.InstrumentRenderer;
import xyz.marsavic.gfxlab.playground.GfxLab;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.objectinstruments.Context;
import xyz.marsavic.objectinstruments.Panel;
import xyz.marsavic.objectinstruments.instruments.InstrumentElement;
import xyz.marsavic.objectinstruments.instruments.InstrumentText;


public class App extends Application {
	
	static {
		System.setProperty("prism.forceGPU=true", "true");
	}
	
	private final GfxLab gfxLab = new GfxLab();
	
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GFX Lab");
		
		Panel panelR = new Panel();
		
		panelR.addInstruments(
				new InstrumentRenderer(gfxLab.sink.out()),
				new InstrumentText(() -> Profiling.infoTextSystem() + Profiling.infoTextProfilers(), 150)
		);
		
		Scene scene = new Scene(panelR.region(), 654, 826);
		scene.getStylesheets().setAll(Resources.stylesheetURL);
		
		primaryStage.getIcons().setAll(Resources.iconsApplication());
		primaryStage.sizeToScene();
		primaryStage.setScene(scene);

		
		primaryStage.setFullScreenExitHint("");
		
		primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			A0 action = switch (event.getCode()) {
				case ESCAPE -> Platform::exit;
				case F11 -> () -> primaryStage.setFullScreen(!primaryStage.isFullScreen());
				default -> A0.NOOP;
			};
			
			action.execute();
		});

		
		primaryStage.show();
	}
}
