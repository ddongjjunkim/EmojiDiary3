package com.diary.ishita.mydiary;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.diary.ishita.mydiary.data.DiaryContract.DiaryEntry;
import com.diary.ishita.mydiary.data.DiaryDbHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,LoaderManager.LoaderCallbacks<Cursor>{

    private  DiaryDbHelper mDbHelper;
    private ListView listView;
    private DiaryCursorAdapter mAdapter;
    private View EmptyView;
    public static TextView user_nav_name;
    public static TextView user_nav_email;
    public static ImageView user_nav_image;
    public static boolean has_set_image = false;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar 생성하기
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //diary 작성 버튼
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                startActivity(intent);

            }
        });

        //좌측 drawer menu
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        // Create and/or open a database to read from it
        // 내용을 읽어오기 위해 db를 연다.
        mDbHelper = new DiaryDbHelper(this);
        listView = (ListView)findViewById(R.id.list_view);
        //
        EmptyView = (View)findViewById(R.id.empty_view);
        listView.setEmptyView(EmptyView);

        mAdapter = new DiaryCursorAdapter(this,null);

        //xml파일에서 배치한 ListView를 참조하기 위해 findViewById 메서드를 호출한 후에 setAdapter 메서드로 아답터를 설정합니다.
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri= ContentUris.withAppendedId(DiaryEntry.CONTENT_URI,id);

                Intent intent= new Intent(MainActivity.this,DetailActivity.class);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        //액티비티와 프레그먼트 사이의 비동기 데이터를 사용하기 위해 loadermanager 사용

        getLoaderManager().initLoader(URL_LOADER,null,this);
        View header=navigationView.getHeaderView(0);
        user_nav_name = (TextView)header.findViewById(R.id.user_name);
        user_nav_email = (TextView)header.findViewById(R.id.user_email);
//        user_nav_image = (ImageView)header.findViewById(R.id.User_photo);
//
//        user_nav_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                   Intent intent = new Intent(MainActivity.this,UserImageActivity.class);
//                   startActivity(intent);
//            }
//        });

        String[] projection =new String[]{DiaryEntry._USER_ID,DiaryEntry.USER_COLUMN_NAME,DiaryEntry.USER_COLUMN_EMAIL};

        //유저 데이터베이스 접근
        Cursor cur = getContentResolver().query(DiaryEntry.USER_CONTENT_URI,projection,null,null,null);
        if(cur.getCount()!=0) {
            cur.moveToFirst();
            user_nav_name.setText(cur.getString(cur.getColumnIndex(DiaryEntry.USER_COLUMN_NAME)));
            user_nav_email.setText(cur.getString(cur.getColumnIndex(DiaryEntry.USER_COLUMN_EMAIL)));
        }

        //이미지데이터 접근
//        String[] projection = new String[]{ DiaryEntry._IMAGE_ID,DiaryEntry.COLUMN_USER_IMAGE_DATA};
//        Cursor c= getContentResolver().query(DiaryEntry.IMAGE_URI,projection,null,null,null);
//        if((c.getCount()!=0)&&(c!=null)){
//            c.moveToFirst();
//            if(c.getBlob(1)!=null) {
//                byte[] image = c.getBlob(1);
//                Bitmap b = DbBitmapUtils.getImage(image);
//                user_nav_image.setImageBitmap(b);
//            }
//        }


    }

    //뒤로가기 버튼 눌렀을 경우를 처리하는 함수
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) { //drawer menu가 열려있을 경우
            drawer.closeDrawer(GravityCompat.START);    //drawer 먼저 닫기
        } else {
            super.onBackPressed();
        }
    }

    //현재 목록에 일기가 있을 경우 true
    public static boolean has_diary= true;

    // Toolbar에 menu.xml을 인플레이트 함;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Toolbar에 추가된 항목의 select 이벤트 처리하는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_all) {
            if(has_diary)
            deleteConfirmationDialog();
            else{
                Toast toast= Toast.makeText(this,"작성된 일기가 없습니다",Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //모두 삭제 버튼 눌렀을 경우 확인메세지 다이얼로그로 띄우기
    private void deleteConfirmationDialog(){
        AlertDialog.Builder builder= new  AlertDialog.Builder(this);
        builder.setMessage("모든 일기를 삭제하시겠습니까?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(DiaryEntry.CONTENT_URI,null,null);
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


//    private void deleteAll() {
//        getContentResolver().delete(DiaryEntry.CONTENT_URI,null,null);
//
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // 목록의 item을 눌렀을 때 navigation view를 handle처리
        int id = item.getItemId();
        switch (id){
            case R.id.user_profile:
             Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
                return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

// 개발자에게 이메일 보내기
//    private void emailIntent() {
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:"));
//        intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback regarding myDiary app");
//        intent.putExtra(Intent.EXTRA_EMAIL,new String[]{"ishitasharma12699@gmail.com"});
//        if(intent.resolveActivity(getPackageManager())!=null){
//            startActivity(intent);
//        }
//
//    }

    //로더 ID = 0
    private final static int URL_LOADER= 0;

    // 다이어리 데이터 로딩하기
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        String[] projection = {DiaryEntry._ID, DiaryEntry.COLUMN_TITLE, DiaryEntry.COLUMN_DATE, DiaryEntry.COLUMN_IMAGE_DATA};
        String[] projection = {DiaryEntry._ID, DiaryEntry.COLUMN_TITLE, DiaryEntry.COLUMN_DATE};
        switch (id){
            case URL_LOADER:
                return new CursorLoader(this,DiaryEntry.CONTENT_URI,projection,null,null,null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if(data.getCount()==0){
            has_diary=false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.swapCursor(null);
    }


    //emojidata.csv file을 불러온다.
    public void openCSV() {
    try {
        InputStream in = this.getResources().openRawResource(R.raw.emojidata);

        BufferedReader buffer = null;
        if (in != null) {
            InputStreamReader stream = new InputStreamReader(in, "utf-8");
            buffer = new BufferedReader(stream);
        }

        String read;
        StringBuilder sb = new StringBuilder("");

        while ((read = buffer.readLine()) != null) {
            sb.append(read);
        }
        in.close();

        System.out.println(sb.toString());

    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

}
}