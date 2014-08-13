package com.globant.labs.swipper;



import com.globant.labs.swipper.utils.Utils;
import com.globant.labs.swipper.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AutoCompleteTextView;


public class ContactoActivity extends ActionBarCustomActivity {

	private String to = "ciudadanosconcodigo@gmail.com";

	private AutoCompleteTextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacto);

		textView = (AutoCompleteTextView) findViewById(R.id.message_body);

	}

	public void send(View view) {
		boolean cancel = false;
		View focusView = null;

		int ecolor = Color.RED; // whatever color you want
		String estring = getString(R.string.error_field_required);
		ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
		SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
		ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);

		// Check for a valid password.
		String name = textView.getText().toString();
		if (TextUtils.isEmpty(name)) {
			textView.setError(ssbuilder);
			focusView = textView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			String subject = "[Consulta Resistenciarte]";
			String message = textView.getText().toString();

			Intent email = new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
			email.putExtra(Intent.EXTRA_SUBJECT, subject);
			email.putExtra(Intent.EXTRA_TEXT, message);

			// need this to prompts email client only
			email.setType("message/rfc822");

			startActivity(Intent.createChooser(email,
					"Elegi tu cliente de E-mail:"));
		}
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, ContactoActivity.class);
		home.startActivity(intent);
	}



}
