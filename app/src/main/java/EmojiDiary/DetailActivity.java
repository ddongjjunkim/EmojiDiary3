package EmojiDiary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.diary.ishita.mydiary.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import EmojiDiary.data.DiaryContract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int RESULT_TITLE_SPEECH = 100;
    private static final int RESULT_DESCRIPTION_TEXT = 101;
    private static EditText weather_text_view;
    private static EditText mood_text_view;
    private static EditText key1_text_view;
    private static TextView title_text_view;
    private static TextView date_text_view;
    private static EditText description_text_view;
    private static Uri CURRENT_DIARY_URI;
    private static final int LOADER_ID= 1;
    private static Button date_range;
    private static Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent =getIntent();
        CURRENT_DIARY_URI=intent.getData();
        if(CURRENT_DIARY_URI==null){
            setTitle("New");
            invalidateOptionsMenu();
        }
        else {
            setTitle("Edit");
            getLoaderManager().initLoader(LOADER_ID,null,this);
        }

        weather_text_view=(EditText) findViewById(R.id.weather_note);
        mood_text_view=(EditText) findViewById(R.id.mood_note);
        key1_text_view=(EditText) findViewById(R.id.key_note_1);
        date_text_view= (TextView)findViewById(R.id.date_detail_activity);
        description_text_view=(EditText)findViewById(R.id.description_detail_Activity);
        date_range =(Button)findViewById(R.id.date_selector);
        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        date_range.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(DetailActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        date_range.setOnTouchListener(mTouchListener);

        weather_text_view.setOnTouchListener(mTouchListener);

        description_text_view.setOnTouchListener(mTouchListener);

    }

        //TEST!!!
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode){
                case RESULT_TITLE_SPEECH:
                    if(resultCode==RESULT_OK&& data!=null){
                        ArrayList<String> text = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        weather_text_view.setText(text.get(0));
                    }
                    break;
                case RESULT_DESCRIPTION_TEXT:
                    if(resultCode==RESULT_OK && data!=null){
                        ArrayList<String> text= data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        description_text_view.setText(text.get(0));
                    }
                    break;
            }
        }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // 새로 작성된 다이어리인 경우 '삭제'버튼 숨기기
        if (CURRENT_DIARY_URI == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note,menu);
        return true;
    }

    private boolean has_filled_diary= false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            has_filled_diary = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("수정사항이 저장되지 않을 수 있습니다!");
        builder.setPositiveButton("삭제", discardButtonClickListener);
        builder.setNegativeButton("계속 작성", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // alert dialog 띄우기
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // 다이어리가 수정되지 않았으면 back button 활성화
        if (!has_filled_diary) {
            super.onBackPressed();
            return;
        }

        // 수정된 내용이 저장되지 않은 채로 back하면 경고메세지 출력
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // unsave 경고 출력
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // 다이어리 삭제 및 취소 메시지, 처리
    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder= new  AlertDialog.Builder(this);
        builder.setMessage("Delete this diary?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDiary();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_save:
                if(CURRENT_DIARY_URI==null)
                saveDiary(DiaryContract.DiaryEntry.CONTENT_URI);
                else
                    saveDiary(CURRENT_DIARY_URI);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    // 다이어리 저장 함수
    private void saveDiary(Uri saveUri) {
        ContentValues values = new ContentValues();

        String title= weather_text_view.getText().toString();
        title = convertToEmoji(title);
        String date = date_text_view.getText().toString();
        String description = description_text_view.getText().toString();

        values.put(DiaryContract.DiaryEntry.COLUMN_TITLE,title);
        values.put(DiaryContract.DiaryEntry.COLUMN_DATE,date);
        values.put(DiaryContract.DiaryEntry.COLUMN_NOTE,description);

        Toast toast;
        String message;
        if(saveUri.equals(DiaryContract.DiaryEntry.CONTENT_URI)){
            Uri uri =getContentResolver().insert(DiaryContract.DiaryEntry.CONTENT_URI,values);
            if(uri!=null)
                message="Note saved!";
            else
                message="Error saving note";
        }
        else{

            int rows= getContentResolver().update(CURRENT_DIARY_URI,values,null,null);
            if(rows!=0)
                message="Note updated!";
            else
                message="Error updating note";
        }
        toast= Toast.makeText(this,message,Toast.LENGTH_SHORT);
        toast.show();

        MainActivity.has_diary=true;
        finish();
    }

    // 다이어리 삭제 함수
    private void deleteDiary() {

        if(CURRENT_DIARY_URI!=null){
            int rows= getContentResolver().delete(CURRENT_DIARY_URI,null,null);
            Toast toast;
            String message;
            if(rows != 0){
                message="일기가 삭제되었습니다";
            }
            else
                message="일기를 삭제할 수 없습니다";
            toast= Toast.makeText(this,message,Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection= {DiaryContract.DiaryEntry._ID, DiaryContract.DiaryEntry.COLUMN_TITLE, DiaryContract.DiaryEntry.COLUMN_DATE, DiaryContract.DiaryEntry.COLUMN_NOTE};
        return new CursorLoader(this, CURRENT_DIARY_URI, projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        update(data);
    }

    private void update(Cursor data) {
        if(data.getCount()==0)
            return;
        else {
            data.moveToFirst();
            String title = data.getString(data.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_TITLE));
            String date = data.getString(data.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_DATE));
            String description = data.getString(data.getColumnIndex(DiaryContract.DiaryEntry.COLUMN_NOTE));
            Log.v("DetailActivity", description);
            weather_text_view.setText(title);
            date_text_view.setText(date);
            description_text_view.setText(description);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void updateLabel() {
        String myFormat = "MMM dd yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        date_text_view.setText(sdf.format(myCalendar.getTime()));
    }

    // String 읽어들이기 위한 객체 생성
    StringBuilder sb = new StringBuilder("");

    // emojidata 파일 불러오기
    public void openFile() {

        // 텍스트 파일 읽어서 buffer에 저장
        try {
            InputStream in = this.getResources().openRawResource(R.raw.emoji_data_1);
            BufferedReader buffer = null;
            if (in != null) {
                InputStreamReader stream = new InputStreamReader(in, "utf-8");
                buffer = new BufferedReader(stream);
            }

            String read;
            while ((read = buffer.readLine()) != null) {
                sb.append(read);
            }
            in.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
    }


    // 사용자에게 받은 텍스트를 이모지로 변환해주는 함수
    public String convertToEmoji(String text) {
        openFile();

        // 콤마 단위로 split해서 배열 만들기
        String stringsb = sb.toString();
        String[] arr1 = stringsb.split(",");
        int len = arr1.length;
        System.out.println("array" + Arrays.toString(arr1));    // 배열로 전체 출력

        // 이모지와 텍스트 배열로 나누기
        String[] emojiarr = new String[len/2];
        String[] textarr = new String[len/2];

        int even = 0;
        int odd = 0;
        for (int i = 0; i < len; i++) {
            if (i % 2 == 0) {
                emojiarr[even] = arr1[i];
//                System.out.println("emoji1: " + emojiarr[i/2]);
                even++;
            } else if (i % 2 != 0) {
                textarr[odd] = arr1[i];
//                System.out.println("text: " + textarr[(i-1)/2]);
                odd++;
            }
        }
        System.out.println("emoji array" + Arrays.toString(emojiarr));  // 이모지 배열 출력
        System.out.println("text array " + Arrays.toString(textarr));   //텍스트 배열 출력

        System.out.println(text);
        for(int i = 0; i < len/2; i++) {
            if(text.equals(textarr[i])) {
                text = emojiarr[i];
            }
        }
        System.out.println(text);
        return text;

    }




 }



