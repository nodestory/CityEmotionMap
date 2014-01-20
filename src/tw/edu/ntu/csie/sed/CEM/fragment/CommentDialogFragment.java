package tw.edu.ntu.csie.sed.CEM.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.rest.APIHelper;
import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CommentDialogFragment extends DialogFragment {
	private ListView mListView;
	private ProgressBar mProgressBar;
	private EditText mEditText;
	private Button mSendButton;
	private RelativeLayout mAddCommentPanel;

	private static String mMessageId;

	public static CommentDialogFragment newInstance(String messageId) {
		mMessageId = messageId;
		CommentDialogFragment fragment = new CommentDialogFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog != null) {
			Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAddCommentPanel.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_view_comments, null);
		builder.setView(view);
		builder.setTitle("觀看留言");
		builder.setPositiveButton("留言", null);
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		mListView = (ListView) view.findViewById(R.id.listiview_comment);
		mListView.setVisibility(View.INVISIBLE);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
		mSendButton = (Button) view.findViewById(R.id.button_send);
		mEditText = (EditText) view.findViewById(R.id.edittext_comment);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
				new APIHelper(getActivity(), new HTTPResponseReceiver() {
					@Override
					public void onSuccess(int api, String result) {
						Log.i(getClass().getName(), result);
					}

					@Override
					public void onError(int resultCode) {
//						TODO
						Toast.makeText(getActivity(),
								"Sorry, the meessage was not sent successfully. Please try agian later!",
								Toast.LENGTH_SHORT).show();
					}
				}).addComment(mMessageId, mEditText.getText().toString(),
						getActivity().getSharedPreferences("CEM", 0).getString("fb_id", ""), "1",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(new Date()));
			}
		});
		mAddCommentPanel = (RelativeLayout) view.findViewById(R.id.layout_add_comment);
		mAddCommentPanel.setVisibility(View.GONE);
		return dialog;
	}

	public void update(String[] messages) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, messages);
		mListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
	}
}