package android.example.com.githubrepos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


//This class is responsible for handnling all the database related operations
//All the repositories are cached locally and saved using this class in an SQLite database
public class DBHelper extends SQLiteOpenHelper {

    public static class CONSTANTS{
        public static final String DATABASE_NAME = "Repositories.db";
        private static final int DATABASE_VERSION = 2;
        public static final String TABLE_NAME = "repositories";
        public static final String COLUMN_REPO_NAME = "repoName";
        public static final String COLUMN_REPO_DESC = "repoDescription";
        public static final String COLUMN_REPO_OWNER = "repoOwner";
        public static final String COLUMN_REPO_URL = "repoURL";
        public static final String COLUMN_REPO_OWNER_URL = "ownerURL";
        public static final String COLUMN_REPO_PAGE_NUMBER = "pageNumber";
        public static final String COLUMN_REPO_FORKED = "forked";
    }

    private static DBHelper dbHelper;
    private static SQLiteDatabase readableDatabase;
    private static SQLiteDatabase writableDatabase;

    public static final String SQL_CREATE_QUERY = "CREATE TABLE " + CONSTANTS.TABLE_NAME
            + " (" + CONSTANTS.COLUMN_REPO_NAME + " TEXT,"
            + CONSTANTS.COLUMN_REPO_DESC + " TEXT,"
            + CONSTANTS.COLUMN_REPO_OWNER + " TEXT,"
            + CONSTANTS.COLUMN_REPO_URL + " TEXT,"
            + CONSTANTS.COLUMN_REPO_OWNER_URL + " TEXT,"
            + CONSTANTS.COLUMN_REPO_PAGE_NUMBER + " INT,"
            + CONSTANTS.COLUMN_REPO_FORKED + " INT" + ")";

    public static final String SQL_DELETE_QUERY = "DROP TABLE IF EXISTS " + CONSTANTS.TABLE_NAME;

    private DBHelper(Context context){
        super(context, CONSTANTS.DATABASE_NAME, null, CONSTANTS.DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context){
        if(dbHelper == null){
            dbHelper = new DBHelper(context.getApplicationContext());
            readableDatabase = dbHelper.getReadableDatabase();
            writableDatabase = dbHelper.getWritableDatabase();
        }
        return dbHelper;
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_QUERY);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_QUERY);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

    //Call this method to insert new repositories to the local database
    public static void insertRepositories(List<Repository> remoteRepositories){
        ContentValues contentValues;
        Repository tempRepo;
        int numberOfRepos = remoteRepositories.size();
        for(int i = 0; i < numberOfRepos; i++){
            contentValues = new ContentValues();
            tempRepo = remoteRepositories.get(i);
            contentValues.put(CONSTANTS.COLUMN_REPO_NAME, tempRepo.getRepoName());
            contentValues.put(CONSTANTS.COLUMN_REPO_DESC, tempRepo.getRepoDescription());
            contentValues.put(CONSTANTS.COLUMN_REPO_OWNER, tempRepo.getRepoOwner());
            contentValues.put(CONSTANTS.COLUMN_REPO_URL, tempRepo.getRepoURL());
            contentValues.put(CONSTANTS.COLUMN_REPO_OWNER_URL, tempRepo.getOwnerURL());
            contentValues.put(CONSTANTS.COLUMN_REPO_PAGE_NUMBER, tempRepo.getPageNumber());
            contentValues.put(CONSTANTS.COLUMN_REPO_FORKED, tempRepo.isForked());
            writableDatabase.insert(CONSTANTS.TABLE_NAME, null, contentValues);
        }
    }

    //Call this method to read the local repositories from the database
    public static List<Repository> getLocalRepos(String owner){
        //The list that will be returned
        List<Repository> localRepositories = new ArrayList<>();

        //First read the data from the database then convert it into a List<Repository>
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {CONSTANTS.COLUMN_REPO_NAME,
                CONSTANTS.COLUMN_REPO_DESC,
                CONSTANTS.COLUMN_REPO_OWNER,
                CONSTANTS.COLUMN_REPO_URL,
                CONSTANTS.COLUMN_REPO_OWNER_URL,
                CONSTANTS.COLUMN_REPO_PAGE_NUMBER,
                CONSTANTS.COLUMN_REPO_FORKED};

        //How you want the results to be sorted in the resulting Cursor
        String sortOrder = CONSTANTS.COLUMN_REPO_NAME + " ASC";

        //The columns for the WHERE clause
        String selection = CONSTANTS.COLUMN_REPO_OWNER + " =?";

        //The values of the WHERE clause
        String[] selectionArguments = {owner};

        //Read the data into a cursor object
        Cursor cursor = readableDatabase.query(CONSTANTS.TABLE_NAME,
                projection, selection, selectionArguments, null, null, sortOrder);

        //Temp variables to hold the data
        Repository tempRepo;
        String repoName;
        String repoDescription;
        String repoOwner;
        int pageNumber;
        String repoURL;
        String ownerURL;
        boolean forked;

        //Column indexes
        int repoNameColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_NAME);
        int repoDescriptionColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_DESC);
        int repoOwnerColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_OWNER);
        int pageNumberColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_PAGE_NUMBER);
        int repoURLColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_URL);
        int ownerURLColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_OWNER_URL);
        int forkableColumnIndex = cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_FORKED);

        //Loop through the cursor object and add the data to the List<Repository>
        if(cursor.moveToFirst()){
            do {
                repoName = cursor.getString(repoNameColumnIndex);
                repoDescription = cursor.getString(repoDescriptionColumnIndex);
                repoOwner = cursor.getString(repoOwnerColumnIndex);
                pageNumber = cursor.getInt(pageNumberColumnIndex);
                repoURL = cursor.getString(repoURLColumnIndex);
                ownerURL = cursor.getString(ownerURLColumnIndex);
                if(cursor.getInt(forkableColumnIndex) == 1){
                    forked = true;
                }
                else{
                    forked = false;
                }
                tempRepo = new Repository(repoName, repoDescription, repoOwner, pageNumber, repoURL, ownerURL, forked);
                localRepositories.add(tempRepo);
            }while (cursor.moveToNext());
        }

        return localRepositories;
    }

    public static void deleteAllRepos(String owner){
        int numOfRows = writableDatabase.delete(CONSTANTS.TABLE_NAME, CONSTANTS.COLUMN_REPO_OWNER + " =? ", new String[]{owner});
        Log.d("DBHelper", "Number of deleted rows: " + numOfRows);
    }

    public static int getMaxPageNumber(String owner){
        int maxPageNumber = 0;
        Cursor cursor = readableDatabase.query(CONSTANTS.TABLE_NAME,
                new String[]{"MAX(" + CONSTANTS.COLUMN_REPO_PAGE_NUMBER + ") as pageNumber"},
                CONSTANTS.COLUMN_REPO_OWNER + " =?",
                new String[] {owner},
                null,
                null,
                null);
        if(cursor.moveToFirst()){
            maxPageNumber = cursor.getInt(cursor.getColumnIndexOrThrow(CONSTANTS.COLUMN_REPO_PAGE_NUMBER));
            return maxPageNumber;
        }
        else{
            return maxPageNumber;
        }
    }
}
