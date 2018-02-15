package com.mualab.org.user.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.mualab.org.user.R;


public class ProgressDialog extends Dialog {

	public Context context;

	public ProgressDialog(Context context) {
		super(context, android.R.style.Theme_Translucent);

		this.context = context;
		// This is the layout XML file that describes your Dialog layout
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.custom_progress_dialog_layout);
	}

}
