package jp.shuri.android.voicememo;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements IMainActivity, ISetString {

    private static final int REQUEST_CODE = 0;

    private static final String KEY_CANDIDATES = "KEY_CANDIDATES";

    private String mInputString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mInputString = "";
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startRecognizerIntent() {
        try {
            // インテント作成
            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "VoiceRecognitionTest"); // お好きな文字に変更できます

            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // このインテントに応答できるアクティビティがインストールされていない場合
            Toast.makeText(this, "ActivityNotFoundException", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 自分が投げたインテントであれば応答する
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "";

            // 結果文字列リスト
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            Bundle args = new Bundle();
            args.putStringArrayList(KEY_CANDIDATES, results);

            FragmentManager manager = getFragmentManager();
            SelectCandidatesDialogFragment dialog = new SelectCandidatesDialogFragment();
            dialog.setArguments(args);
            dialog.show(manager, "dialog");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setString(String str) {
        mInputString += str;

        // debug
        Toast.makeText(this, mInputString, Toast.LENGTH_LONG).show();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Button btn = (Button)rootView.findViewById(R.id.button_main);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((IMainActivity)getActivity()).startRecognizerIntent();
                }
            });

            return rootView;
        }
    }

    public static class SelectCandidatesDialogFragment extends DialogFragment {
        private ArrayList<String> mCandidates;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mCandidates = getArguments().getStringArrayList(KEY_CANDIDATES);

            ListView lv = new ListView(getActivity());
            lv.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mCandidates));
            lv.setScrollingCacheEnabled(false);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ((ISetString)getActivity()).setString(mCandidates.get(i).toString());
                    dismiss();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("候補を選択");
            builder.setNegativeButton("Cancel", null);
            builder.setView(lv);
            builder.setCancelable(false);

            return builder.create();
        }
    }
}
