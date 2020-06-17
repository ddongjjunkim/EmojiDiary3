package EmojiDiary.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

// ContentProvider.class: app과 db 사이에서 데이터 접근을 쉽게 하도록 관리해주는 클래스
public class DiaryProvider extends ContentProvider {

    private DiaryDbHelper mDiaryHelper;
    private UserDbHelper mUserDbHelper;

    // 각 uri 경로에 다른 코드
    private static final int DIARY = 100;
    private static final int DIARY_ITEM = 101;
    private static final int USER = 102;
    private static final int USER_ITEM = 103;

    // UriMatcher: 두개의 uri값을 비교하여 해당하는(약속된) 값을 출력해주는 기능을 하는 class
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        // addURI(): 약속된 값을 등록할 때 사용
        mUriMatcher.addURI(DiaryContract.CONTENT_AUTHORITY, DiaryContract.PATH_DIARY, DIARY);
        mUriMatcher.addURI(DiaryContract.CONTENT_AUTHORITY,DiaryContract.PATH_DIARY+"/#", DIARY_ITEM);

        mUriMatcher.addURI(DiaryContract.CONTENT_AUTHORITY, DiaryContract.USER, USER);
        mUriMatcher.addURI(DiaryContract.CONTENT_AUTHORITY,DiaryContract.USER+"/#", USER_ITEM);
    }

    @Override
    public boolean onCreate() {

        mDiaryHelper = new DiaryDbHelper(getContext());
        mUserDbHelper= new UserDbHelper(getContext());

        return true;
    }


    @Override
    public Cursor query( Uri uri, String[] projection,  String selection,  String[] selectionArgs,  String sortOrder){

        //Readable database 불러오기
        SQLiteDatabase dDB = mDiaryHelper.getReadableDatabase();    // 다이어리 db
        SQLiteDatabase uDB = mUserDbHelper.getReadableDatabase();   // 사용자 db
        Cursor cursor = null;   // cursor 정의

        // match(): 새로운 URI 가져와서 등록된 URI와 비교하여 해당하는 정수값을 반환
        final int match = mUriMatcher.match(uri);

        switch (match){

            case DIARY:
                cursor= dDB.query(DiaryContract.DiaryEntry.TABLE_NAME, projection, selection, selectionArgs,null,null,sortOrder);
                break;
            case DIARY_ITEM:
                selection= DiaryContract.DiaryEntry._ID+"=?";
                selectionArgs= new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor= dDB.query(DiaryContract.DiaryEntry.TABLE_NAME, projection, selection, selectionArgs,null,null,sortOrder);
                break;
            case USER:
                Log.v("before query", uri.toString());
                cursor= uDB.query(DiaryContract.DiaryEntry.USER_TABLE_NAME, projection, selection, selectionArgs,null,null,sortOrder);
                return cursor;
            case USER_ITEM:
                selection= DiaryContract.DiaryEntry._USER_ID+"=?";
                selectionArgs= new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor= uDB.query(DiaryContract.DiaryEntry.USER_TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI "+uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Override
    public String getType( Uri uri) {

        final int match= mUriMatcher.match(uri);

        switch (match){
            case DIARY:
                return DiaryContract.DiaryEntry.CONTENT_LIST_TYPE;
            case DIARY_ITEM:
                return DiaryContract.DiaryEntry.CONTENT_ITEM_TYPE;
            case USER:
                return DiaryContract.DiaryEntry.USER_LIST_TYPE;
            case USER_ITEM:
                return DiaryContract.DiaryEntry.USER_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("unknown uri "+uri);
        }

    }

    @Override
    public Uri insert( Uri uri, ContentValues values) {

        final int match = mUriMatcher.match(uri);

        switch (match){
            case DIARY:
                return insertNote(uri,values);
            default:
                throw new IllegalArgumentException("Cannot perform insert on unknown URI "+uri);
        }

    }

    private Uri insertNote(Uri uri, ContentValues values) {

        SQLiteDatabase dDB = mDiaryHelper.getReadableDatabase();
        long id= dDB.insert(DiaryContract.DiaryEntry.TABLE_NAME,null, values);
        if(id != 0)
            getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);

    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {

        // Writable database 불러오기
        SQLiteDatabase dDB = mDiaryHelper.getWritableDatabase();
        SQLiteDatabase uDB = mUserDbHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int rows;
        switch (match){
            case DIARY:
                rows = dDB.delete(DiaryContract.DiaryEntry.TABLE_NAME, selection, selectionArgs);
                if(rows != 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            case DIARY_ITEM:
                selection = DiaryContract.DiaryEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rows= dDB.delete(DiaryContract.DiaryEntry.TABLE_NAME, selection, selectionArgs);
                if(rows!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            case USER:
                rows = uDB.delete(DiaryContract.DiaryEntry.USER_TABLE_NAME, selection, selectionArgs);
                if(rows!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            case USER_ITEM:
                selection = DiaryContract.DiaryEntry._USER_ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rows = uDB.delete(DiaryContract.DiaryEntry.USER_TABLE_NAME, selection, selectionArgs);
                if(rows != 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            default:
                throw new IllegalArgumentException("cannot perform delete on unknown URI "+ uri);

        }
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if(values.size()==0){
            return 0;
        }

        // Writable database 불러오기
        SQLiteDatabase dDB = mDiaryHelper.getWritableDatabase();
        SQLiteDatabase uDB = mUserDbHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int rows;
        switch (match){
            case DIARY:
                rows = dDB.update(DiaryContract.DiaryEntry.TABLE_NAME, values, selection, selectionArgs);
                if(rows != 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            case DIARY_ITEM:
                selection = DiaryContract.DiaryEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rows = dDB.update(DiaryContract.DiaryEntry.TABLE_NAME, values, selection, selectionArgs);
                if(rows != 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            case USER:
                rows = uDB.update(DiaryContract.DiaryEntry.USER_TABLE_NAME, values, selection, selectionArgs);
                if(rows != 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
            case USER_ITEM:
                selection = DiaryContract.DiaryEntry._USER_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rows= uDB.update(DiaryContract.DiaryEntry.USER_TABLE_NAME, values, selection, selectionArgs);
                return rows;
            default:
                 throw new IllegalArgumentException("Cannot update the unknown URI "+uri);
        }

    }
}