package com.diary.ishita.mydiary.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DiaryContract {

    public static final String CONTENT_AUTHORITY = "com.diary.ishita.mydiary";  //content provider의 고유이름
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY );
    public static final String PATH_DIARY = "diary";
    public static final String USER ="user";

    private DiaryContract(){
    }

    public static final class DiaryEntry implements BaseColumns{

        // 다이어리 데이터베이스 생성
        public static final Uri CONTENT_URI= Uri.withAppendedPath(BASE_CONTENT_URI,PATH_DIARY);
        public static final String TABLE_NAME= "diary";
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+ "/" + CONTENT_AUTHORITY+ "/" +PATH_DIARY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ PATH_DIARY;

        // 다이어리 테이블 정의
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";  // 제목
        public static final String COLUMN_DATE ="date";     // 날짜
        public static final String COLUMN_NOTE = "note";  // 일기

        // 사용자 데이터베이스 생성
        public static final Uri USER_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,USER);
        public static final String USER_TABLE_NAME ="user";
        public static final String USER_LIST_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY+"/"+USER;
        public static final String USER_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ USER;

        //사용자 테이블 정의
        public static final String _USER_ID = BaseColumns._ID;
        public static final String USER_COLUMN_NAME = "title";
        public static final String USER_COLUMN_EMAIL ="date";

    }
}
