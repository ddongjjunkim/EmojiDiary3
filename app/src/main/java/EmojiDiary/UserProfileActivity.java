package EmojiDiary;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.diary.ishita.mydiary.R;

import EmojiDiary.data.DiaryContract;

public class UserProfileActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static EditText  user_name;
    private static EditText user_email;
    private static boolean has_saved= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setTitle("My Profile");

        user_name=(EditText)findViewById(R.id.user_name_edit_text_view);
        user_email=(EditText)findViewById(R.id.user_email_edit_text_view);

        String[] projection = {DiaryContract.DiaryEntry._USER_ID, DiaryContract.DiaryEntry.USER_COLUMN_NAME, DiaryContract.DiaryEntry.USER_COLUMN_EMAIL};
        Cursor cursor =getContentResolver().query(DiaryContract.DiaryEntry.USER_CONTENT_URI,projection,null,null,null);
        if(cursor.getCount()!=0) {
            getLoaderManager().initLoader(URL_LOADER, null, this);
            has_saved = true;
        }
        else{
            invalidateOptionsMenu();
        }

    }

    // 첫번째 프로필 작성일 때에는 삭제 아이콘 숨기기
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!has_saved) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    // 이미 프로필이 있을 때에는 삭제 아이콘 보이도록 action bar에 menu를 inflate하기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note, menu);
        return true;
    }

    // Action bar 아이템 클릭을 handle함.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // 프로필 삭제 시 확인 메세지
        if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }

        // 프로필 저장
        else if (id == R.id.action_save){
                save();
            finish();
            return true;
        }

        // 홈 버튼 누를 시 종료
        else if(id==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    // 프로필 저장 함수
    private void save() {
        // ContentValues: ContentResolver가 처리할 수 있는 값의 집합 저장
        // 1. 객체 생성
        ContentValues values = new ContentValues();
        String name = user_name.getText().toString();
        String email = user_email.getText().toString();
//        String notes = user_note.getText().toString();

        //2. put()로 항목, 값을 DB테이블 순서에 맞게 집어넣음.
        values.put(DiaryContract.DiaryEntry.USER_COLUMN_NAME,name);
        values.put(DiaryContract.DiaryEntry.USER_COLUMN_EMAIL,email);
//        values.put(DiaryEntry.USER_COLUMN_NOTES,notes);
        Toast toast;
        String message = null;

        // 프로필 처음 등록 시, DB에 새로 저장 후 저장 메세지 출력
        if(!has_saved) {
            Uri uri = getContentResolver().insert(DiaryContract.DiaryEntry.USER_CONTENT_URI, values);
            if(uri != null)
                has_saved = true;
            message = "프로필이 저장되었습니다!";
        }
        // 기존 프로필 업데이트 시, DB 업데이트 후 업데이트 메세지
        else{
            int rows = getContentResolver().update(DiaryContract.DiaryEntry.USER_CONTENT_URI, values,null,null);
            if(rows != 0)
                message="프로필이 업데이트되었습니다!";
        }
              toast= Toast.makeText(this,message,Toast.LENGTH_SHORT);
        toast.show();

        MainActivity.user_nav_name.setText(name);
        MainActivity.user_nav_email.setText(email);
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder= new  AlertDialog.Builder(this);
        builder.setMessage(" 프로필을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){

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

    //프로필 삭제 함수
    private void delete() {
        //DB에서 프로필 삭제
        getContentResolver().delete(DiaryContract.DiaryEntry.USER_CONTENT_URI,null,null);

        Toast toast= Toast.makeText(this,"프로필이 삭제되었습니다",Toast.LENGTH_SHORT);
        toast.show();

        MainActivity.user_nav_name.setText("나만의 프로필을 등록하세요!");
        MainActivity.user_nav_email.setText("e-mail");
        has_saved = false;
        finish();
    }


    private final static int URL_LOADER = 2;
    // ID가 2인 로더를 생성하여 넘겨주기
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // 컨텐트에서 가져올 정보들 string에 담아두기
        String[] projection = {DiaryContract.DiaryEntry._USER_ID, DiaryContract.DiaryEntry.USER_COLUMN_NAME, DiaryContract.DiaryEntry.USER_COLUMN_EMAIL};
        switch(id){
            case URL_LOADER:
                // CursorLoader(현재 액티비리, 가져올 컨텐트의 uri, 가져올 정보; null이면 모든 컬럼, 가져올 데이터 필터링하는 정보; 모든 데이터, , 정렬 순)
                // 현재 액티비티로 user content의 uri를 가져옴; ID, name, email 가져오기
                return new CursorLoader(this, DiaryContract.DiaryEntry.USER_CONTENT_URI, projection,null,null,null);
            default:
                return null;
        }
    }


    // 로딩이 끝난 후 cursor를 맨 앞으로 보내기
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()==0){
            return;
        }
        data.moveToFirst();
        user_name.setText(data.getString(data.getColumnIndex(DiaryContract.DiaryEntry.USER_COLUMN_NAME)));
        user_email.setText(data.getString(data.getColumnIndex(DiaryContract.DiaryEntry.USER_COLUMN_EMAIL)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
