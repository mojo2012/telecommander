package at.spot.a1telecommander.ui.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import at.spot.a1telecommander.R;

public class ViewHelper {
	final static String	TAG	= "A1Telecommander/ViewHelper";

	public static void showDialogBox(String caption, String text,
			Context context) {
		// set up dialog
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.dialogbox);
		dialog.setTitle(caption);
		dialog.setCancelable(true);
		// there are a lot of settings, for dialog, check them all out!

		// set up text
		TextView textView = (TextView) dialog.findViewById(R.id.dialog_text);
		textView.setText(text);

		// set up image view
		// ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);
		// img.setImageResource(R.drawable.nista_logo);

		// set up button
		Button button = (Button) dialog.findViewById(R.id.dialog_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		// now that the dialog is set up, it's time to show it
		dialog.show();
	}

	public static void setBackgroundColor(View widget, String color) {
		applyFilterToDrawable(widget.getBackground(), color);
	}

	public static void applyFilterToDrawable(Drawable drawable, String color) {
		PorterDuffColorFilter filter = new PorterDuffColorFilter(Color
				.parseColor(color), PorterDuff.Mode.MULTIPLY);
		drawable.setColorFilter(filter);
	}
}
