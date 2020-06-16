package EmojiDiary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME ="user_database.db";
    private static final int DATABASE_VERSION = 1;
    public static final String USER_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + DiaryContract.DiaryEntry.USER_TABLE_NAME+
            "("+ DiaryContract.DiaryEntry._USER_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            DiaryContract.DiaryEntry.USER_COLUMN_NAME+ " TEXT DEFAULT '사용자 없음'," +
            DiaryContract.DiaryEntry.USER_COLUMN_EMAIL +" TEXT DEFAULT 'e-mail 정보 없음')";

    public UserDbHelper(Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String Query = "DROP TABLE IF EXISTS "+ DiaryContract.USER;
        db.execSQL(Query);
        onCreate(db);
    }
}