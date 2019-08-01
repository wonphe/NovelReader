package top.ox16.yuedu.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import top.ox16.yuedu.bean.BookmarkBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BOOKMARK_BEAN".
*/
public class BookmarkBeanDao extends AbstractDao<BookmarkBean, Long> {

    public static final String TABLENAME = "BOOKMARK_BEAN";

    /**
     * Properties of entity BookmarkBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property NoteUrl = new Property(1, String.class, "noteUrl", false, "NOTE_URL");
        public final static Property BookName = new Property(2, String.class, "bookName", false, "BOOK_NAME");
        public final static Property ChapterName = new Property(3, String.class, "chapterName", false, "CHAPTER_NAME");
        public final static Property ChapterIndex = new Property(4, Integer.class, "chapterIndex", false, "CHAPTER_INDEX");
        public final static Property PageIndex = new Property(5, Integer.class, "pageIndex", false, "PAGE_INDEX");
        public final static Property Content = new Property(6, String.class, "content", false, "CONTENT");
    }


    public BookmarkBeanDao(DaoConfig config) {
        super(config);
    }
    
    public BookmarkBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BOOKMARK_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"NOTE_URL\" TEXT," + // 1: noteUrl
                "\"BOOK_NAME\" TEXT," + // 2: bookName
                "\"CHAPTER_NAME\" TEXT," + // 3: chapterName
                "\"CHAPTER_INDEX\" INTEGER," + // 4: chapterIndex
                "\"PAGE_INDEX\" INTEGER," + // 5: pageIndex
                "\"CONTENT\" TEXT);"); // 6: content
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BOOKMARK_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, BookmarkBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(2, noteUrl);
        }
 
        String bookName = entity.getBookName();
        if (bookName != null) {
            stmt.bindString(3, bookName);
        }
 
        String chapterName = entity.getChapterName();
        if (chapterName != null) {
            stmt.bindString(4, chapterName);
        }
 
        Integer chapterIndex = entity.getChapterIndex();
        if (chapterIndex != null) {
            stmt.bindLong(5, chapterIndex);
        }
 
        Integer pageIndex = entity.getPageIndex();
        if (pageIndex != null) {
            stmt.bindLong(6, pageIndex);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(7, content);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, BookmarkBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(2, noteUrl);
        }
 
        String bookName = entity.getBookName();
        if (bookName != null) {
            stmt.bindString(3, bookName);
        }
 
        String chapterName = entity.getChapterName();
        if (chapterName != null) {
            stmt.bindString(4, chapterName);
        }
 
        Integer chapterIndex = entity.getChapterIndex();
        if (chapterIndex != null) {
            stmt.bindLong(5, chapterIndex);
        }
 
        Integer pageIndex = entity.getPageIndex();
        if (pageIndex != null) {
            stmt.bindLong(6, pageIndex);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(7, content);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public BookmarkBean readEntity(Cursor cursor, int offset) {
        BookmarkBean entity = new BookmarkBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // noteUrl
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // bookName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // chapterName
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // chapterIndex
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // pageIndex
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // content
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, BookmarkBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setNoteUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setBookName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setChapterName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setChapterIndex(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setPageIndex(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setContent(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(BookmarkBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(BookmarkBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(BookmarkBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
