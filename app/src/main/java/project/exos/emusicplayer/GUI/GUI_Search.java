package project.exos.emusicplayer.GUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import project.exos.emusicplayer.Adapter.SongDisplayAdapter;
import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.R;

public class GUI_Search extends AppCompatActivity {
    static ArrayList<SongDisplay> arrayList;
    static boolean needUpdate = false; // note: observer flag
    Handler handler = new Handler();
    TextView editText_SearchInput;
    ListView list_SearchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(DataRepository.GetCurrentThemeId());
        setContentView(R.layout.gui_search);
        editText_SearchInput = findViewById(R.id.editText_SearchInput);
        list_SearchResult = findViewById(R.id.list_SearchResult);
        list_SearchResult.setBackgroundResource(
                (DataRepository.GetCurrentTheme().second) ?
                        R.color.darkThemeBackground :
                        R.color.white

        );
        TypedArray typedArray = obtainStyledAttributes(DataRepository.GetCurrentThemeId(), new int[]{android.R.attr.tint});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        editText_SearchInput.setHintTextColor(
                (color == Color.BLACK) ?
                        getResources().getColor(R.color.blackHint) :
                        getResources().getColor(R.color.whiteHint)

        );

        editText_SearchInput.setSelectAllOnFocus(true);
        editText_SearchInput.requestFocus();
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(editText_SearchInput, InputMethodManager.SHOW_FORCED);

        editText_SearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (editText_SearchInput.getText().toString().equals("")) {
                    if (arrayList != null) arrayList.clear();
                    needUpdate = true;
                    return;
                }
                DataRepository.GetSongByKeyword(editText_SearchInput.getText().toString(), new DataRepository.Callback() {
                    @Override
                    public void ApplyChanges() {

                    }

                    @Override
                    public void ReturnList(ArrayList<SongDisplay> list) {
                        GUI_Search.arrayList = list;
                        GUI_Search.needUpdate = true;
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        editText_SearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (editText_SearchInput.getText().toString().equals("")) {
                        if (arrayList != null) arrayList.clear();
                        needUpdate = true;
                        return true;
                    }
                    DataRepository.GetSongByKeyword(editText_SearchInput.getText().toString(), new DataRepository.Callback() {
                        @Override
                        public void ApplyChanges() {

                        }

                        @Override
                        public void ReturnList(ArrayList<SongDisplay> list) {
                            GUI_Search.arrayList = list;
                            GUI_Search.needUpdate = true;
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        Timer();
    }

    void Timer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (needUpdate) {
                    UpdateGUI();
                    needUpdate = false;
                }
                Timer();
            }
        }, 100);
    }

    void UpdateGUI() {
        if (arrayList != null && arrayList.size() != 0) {
            list_SearchResult.setVisibility(View.VISIBLE);
            SongDisplayAdapter adapter = new SongDisplayAdapter(GUI_Search.this, arrayList);
            list_SearchResult.setAdapter(adapter);
            list_SearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    DataRepository.AddToNewQueueAndPlay(arrayList, arrayList.get(i).getPath());
                }
            });
            list_SearchResult.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // TODO: show menu
                    return true;
                }
            });
        } else {
            list_SearchResult.setVisibility(View.INVISIBLE);
        }
    }

    public void OnButtonClick(View view) {
        // note: this only use by back button
        supportFinishAfterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
