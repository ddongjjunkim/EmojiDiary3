package EmojiDiary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// SQLiteOpenHelper class는 데이터베이스를 생성하고 해당 데이터베이스의 버전을 관리함.
public class DiaryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME ="diary_database.db";
    private static final int DATABASE_VERSION = 1;
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+ DiaryContract.DiaryEntry.TABLE_NAME+
                                               "("+ DiaryContract.DiaryEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                                  DiaryContract.DiaryEntry.COLUMN_TITLE+ " TEXT DEFAULT 'Unknown'," +
                                                  DiaryContract.DiaryEntry.COLUMN_DATE+" TEXT DEFAULT CURRENT_DATE, "+
                                                  DiaryContract.DiaryEntry.COLUMN_NOTE+" TEXT DEFAULT 'empty note' )";

    public DiaryDbHelper(Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    // 데이터베이스가 처음 생성될 때 호출
    @Override
    public void onCreate(SQLiteDatabase db) {
             db.execSQL(CREATE_TABLE);
    }

    //데이터베이스가 업그레이드될 때 호출
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}