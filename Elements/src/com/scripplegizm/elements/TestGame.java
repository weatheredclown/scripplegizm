package com.scripplegizm.elements;


public class TestGame {
/*	
	public static void test() {
		TestGame test = new TestGame();
		test.testNoFarmer();
		test.testGame();
		test.testStarveAll();
	}
	
	BlobBuilder builder;
	BlobFarmer farmer0 = null;
	private void testNoFarmer() {
		switch (ElementsView.turns) {
		case 0:
			ElementsView.player0.AddGatherer(ElementsView.cell0,
					ElementsView.player0.getFoodColor());
			ElementsView.player0.AddGatherer(ElementsView.cell0,
					ElementsView.player0.getFoodColor());
			break;
		case 1:
			ElementsView.player0.AddToxicCollector(ElementsView.cell0);
			builder = ElementsView.player0.AddBuilder(ElementsView.cell0);
			builder.BuildStorehouse(ColorRole.TOXIC);
			break;
		case 2:
			break;
		case 8:
			builder.BuildStorehouse(ColorRole.TOXIC);
			break;
		case 16:
			factory = builder
					.BuildFactory(WorldColor.AZURE, WorldColor.CORAL,
							ElementsView.player0.getFoodColor());
			break;
		case 24:
			ElementsView.player0.AddFactoryWorker(ElementsView.cell0, factory);
			break;
		}
	}
	boolean testGameFirst = true;
	BlobBuilder testGameBuilder = null;

	private void testGame() {
		switch (ElementsView.turns) {
		case 0: {
			farmer0 = ElementsView.player0.AddFarmer(ElementsView.cell0);
			ElementsView.player0.AddFarmer(ElementsView.cell0);
			ElementsView.player2.AddFarmer(ElementsView.cell0);
			ElementsView.player1.AddFarmer(ElementsView.cell1);
			break;
		}
		case 1: {
			for (int i = 0; i < 25; i++) {
				ElementsView.player2.AddBlob(ElementsView.cell0);
			}

			BlobBuilder b = ElementsView.player1.AddBuilder(ElementsView.cell1);
			b.BuildStorehouse(ColorRole.TOXIC);

			testGameBuilder = ElementsView.player0.AddBuilder(ElementsView.cell0);
			if (testGameBuilder != null)
				testGameBuilder.BuildStorehouse(ColorRole.FOOD);
			break;
		}
		case 2: {
			BlobPaver paver = ElementsView.player0.AddPaver(ElementsView.cell0);
			if (paver != null)
				paver.MakeRoad(ElementsView.cell0, ElementsView.cell1);

			ElementsView.player0.AddGatherer(ElementsView.cell0, WorldColor.AQUAMARINE);
			ElementsView.player0.AddGatherer(ElementsView.cell0, WorldColor.CHARCOAL);
			break;
		}
		default:
		}
	}

	BlobBuilder builderA = null;

	private void testStarveAll() {
		switch (ElementsView.turns) {
		case 0:
			ElementsView.player0.AddFarmer(ElementsView.cell0);
			break;
		case 1:
			builderA = ElementsView.player0.AddBuilder(ElementsView.cell0);
			break;
		case 2:
			builderA.BuildStorehouse(ColorRole.FOOD);
			break;
		case 8:
			ElementsView.player1.AddBlob(ElementsView.cell0);
			break;
		case 16:
			ElementsView.player0.AddBlob(ElementsView.cell0);
			ElementsView.player0.AddBlob(ElementsView.cell0);
			ElementsView.player0.AddBlob(ElementsView.cell0);
			ElementsView.player0.AddBlob(ElementsView.cell0);
			ElementsView.player0.AddBlob(ElementsView.cell0);
			ElementsView.player0.AddBlob(ElementsView.cell0);
		}
	}

	Factory factory = null;
*/
}
