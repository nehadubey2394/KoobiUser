package com.mualab.org.user.util.transformers;

import android.graphics.Color;
import android.view.View;

public class DefaultTransformer extends BasePageTransformer {

	public enum Content {
		ONE("1", Color.GREEN),
		TWO("2", Color.YELLOW),
		THREE("3", Color.RED),
		FOUR("4", Color.BLUE),
		FIVE("5", Color.CYAN),
		SIX("6", Color.LTGRAY),
		SEVEN("7", Color.MAGENTA),
		EIGHT("8", Color.DKGRAY);

		private final String mText;
		private final int mColor;

		Content(final String text, final int color) {
			mText = text;
			mColor = color;
		}

		public String getText() {
			return mText;
		}

		public int getColor() {
			return mColor;
		}
	}

	public static boolean isShowBackgroundColor;

	@Override
	public void transformPage(final View page, final int pageIndex, final float position) {

		//if(isShowBackgroundColor)
			//page.setBackgroundColor(DefaultTransformer.Content.values()[pageIndex].getColor());
	}
}
