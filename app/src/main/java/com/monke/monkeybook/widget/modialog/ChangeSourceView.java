package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.model.UpLastChapterModel;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceView {
    public static SavedSource savedSource = new SavedSource();
    private TextView atvTitle;
    private ImageButton ibtStop;
    private RefreshRecyclerView rvSource;
    private MoDialogHUD moDialogHUD;
    private MoDialogView moDialogView;
    private OnClickSource onClickSource;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookShelfBean book;
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private CompositeDisposable compositeDisposable;

    private ChangeSourceView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
        adapter = new ChangeSourceAdapter(context, false);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setOnItemClickListener((view, index) -> {
            moDialogHUD.dismiss();
            onClickSource.changeSource(adapter.getSearchBookBeans().get(index));
        });
        adapter.setOnItemLongClickListener((view, pos) -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenu().add(0, 0, 1, "禁用书源");
            popupMenu.getMenu().add(0, 0, 2, "删除书源");
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                final String url = adapter.getSearchBookBeans().get(pos).getTag();
                BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(url);
                DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().delete(adapter.getSearchBookBeans().get(pos));
                adapter.getSearchBookBeans().remove(pos);
                adapter.notifyItemRemoved(pos);
                if (sourceBean != null) {
                    switch (menuItem.getOrder()) {
                        case 1:
                            sourceBean.setEnable(false);
                            BookSourceManager.addBookSource(sourceBean);
                            BookSourceManager.refreshBookSource();
                            break;
                        case 2:
                            BookSourceManager.removeBookSource(sourceBean);
                            break;
                    }
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        View viewRefreshError = LayoutInflater.from(context).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.setBackgroundResource(R.color.background_card);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            reSearchBook();
        });
        rvSource.setNoDataAndrRefreshErrorView(LayoutInflater.from(context).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);

        SearchBookModel.OnSearchListener searchListener = new SearchBookModel.OnSearchListener() {

            @Override
            public void searchSourceEmpty() {
                Toast.makeText(context, "没有选中任何书源", Toast.LENGTH_SHORT).show();
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true, false);
            }

            @Override
            public void resetSearchBook() {
                ibtStop.setVisibility(View.VISIBLE);
                adapter.reSetSourceAdapter();
            }

            @Override
            public void searchBookFinish() {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true, false);
            }

            @Override
            public boolean checkExists(SearchBookBean searchBook) {
                Boolean result = false;
                for (int i = 0; i < adapter.getICount(); i++) {
                    if (adapter.getSearchBookBeans().get(i).getNoteUrl().equals(searchBook.getNoteUrl()) && adapter.getSearchBookBeans().get(i).getTag().equals(searchBook.getTag())) {
                        result = true;
                        break;
                    }
                }
                return result;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                addSearchBook(value);
            }

            @Override
            public void searchBookError() {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(false);
            }

            @Override
            public int getItemCount() {
                return adapter.getItemCount();
            }
        };
        searchBookModel = new SearchBookModel(context, searchListener, true);
    }

    public static ChangeSourceView getInstance(MoDialogView moDialogView) {
        return new ChangeSourceView(moDialogView);
    }

    void showChangeSource(BookShelfBean bookShelf, final OnClickSource onClickSource, MoDialogHUD moDialogHUD) {
        this.moDialogHUD = moDialogHUD;
        this.onClickSource = onClickSource;
        compositeDisposable = new CompositeDisposable();
        book = bookShelf;
        bookTag = bookShelf.getTag();
        bookName = bookShelf.getBookInfoBean().getName();
        bookAuthor = bookShelf.getBookInfoBean().getAuthor();
        shelfLastChapter = BookshelfHelp.guessChapterNum(bookShelf.getLastChapterName());
        atvTitle.setText(String.format("%s (%s)", bookName, bookAuthor));
        rvSource.startRefresh();
        getSearchBookInDb(bookShelf);
        RxBus.get().register(this);
    }

    private void stopChangeSource() {
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.stopSearch(true);
        }
    }

    void onDestroy() {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.stopSearch(true);
            searchBookModel.shutdownSearch();
        }
    }

    private void getSearchBookInDb(BookShelfBean bookShelf) {
        Single.create((SingleOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor)).build().list();
            if (searchBookBeans == null) searchBookBeans = new ArrayList<>();
            List<SearchBookBean> searchBookList = new ArrayList<>();
            List<BookSourceBean> bookSourceList = new ArrayList<>(BookSourceManager.getSelectedBookSource());
            if (bookSourceList.size() > 0) {
                for (BookSourceBean bookSourceBean : BookSourceManager.getSelectedBookSource()) {
                    boolean hasSource = false;
                    for (SearchBookBean searchBookBean : new ArrayList<>(searchBookBeans)) {
                        if (Objects.equals(searchBookBean.getTag(), bookSourceBean.getBookSourceUrl())) {
                            bookSourceList.remove(bookSourceBean);
                            searchBookList.add(searchBookBean);
                            hasSource = true;
                            break;
                        }
                    }
                    if (hasSource) {
                        bookSourceList.remove(bookSourceBean);
                    }
                }
                UpLastChapterModel.getInstance().startUpdate(searchBookList);
            }
            if (searchBookList.size() > 0) {
                for (SearchBookBean searchBookBean : searchBookList) {
                    if (searchBookBean.getTag().equals(bookShelf.getTag())) {
                        searchBookBean.setIsCurrentSource(true);
                    } else {
                        searchBookBean.setIsCurrentSource(false);
                    }
                }
                Collections.sort(searchBookList, this::compareSearchBooks);
            }
            e.onSuccess(searchBookList);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<SearchBookBean> searchBookBeans) {
                        if (searchBookBeans.size() > 0) {
                            adapter.addAllSourceAdapter(searchBookBeans);
                            ibtStop.setVisibility(View.INVISIBLE);
                            rvSource.finishRefresh(true, true);
                        } else {
                            reSearchBook();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        reSearchBook();
                    }
                });
    }

    private void reSearchBook() {
        rvSource.startRefresh();
        searchBookModel.stopSearch(true);
        searchBookModel.initSearchEngineS(BookSourceManager.getSelectedBookSource());
        searchBookModel.setSearchEngineChanged();
        int id = (int) System.currentTimeMillis();
        searchBookModel.startSearch(id, bookName);
    }

    private synchronized void addSearchBook(List<SearchBookBean> value) {
        if (value.size() > 0) {
            Collections.sort(value, this::compareSearchBooks);
            for (SearchBookBean searchBookBean : value) {
                if (searchBookBean.getName().equals(bookName)
                        && (searchBookBean.getAuthor().equals(bookAuthor) || TextUtils.isEmpty(searchBookBean.getAuthor()) || TextUtils.isEmpty(bookAuthor))) {
                    if (searchBookBean.getTag().equals(bookTag)) {
                        searchBookBean.setIsCurrentSource(true);
                    } else {
                        searchBookBean.setIsCurrentSource(false);
                    }
                    boolean saveBookSource = false;
                    BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(searchBookBean.getTag());
                    if (searchBookBean.getSearchTime() < 60 && bookSourceBean != null) {
                        bookSourceBean.increaseWeight(100 / (10 + searchBookBean.getSearchTime()));
                        saveBookSource = true;
                    }
                    if (shelfLastChapter > 0 && bookSourceBean != null) {
                        int lastChapter = BookshelfHelp.guessChapterNum(searchBookBean.getLastChapter());
                        if (lastChapter > shelfLastChapter) {
                            bookSourceBean.increaseWeight(100);
                            saveBookSource = true;
                        }
                    }
                    if (saveBookSource) {
                        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
                    }
                    handler.post(() -> adapter.addSourceAdapter(searchBookBean));
                    break;
                }
            }
        }
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.mo_dialog_change_source, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        atvTitle = moDialogView.findViewById(R.id.atv_title);
        ibtStop = moDialogView.findViewById(R.id.ibt_stop);
        rvSource = moDialogView.findViewById(R.id.rf_rv_change_source);
        ibtStop.setVisibility(View.INVISIBLE);

        rvSource.setBaseRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> stopChangeSource());

    }

    private int compareSearchBooks(SearchBookBean s1, SearchBookBean s2) {
        boolean s1tag = s1.getTag().equals(bookTag);
        boolean s2tag = s2.getTag().equals(bookTag);
        if (s2tag && !s1tag)
            return 1;
        else if (s1tag && !s2tag)
            return -1;
        int result = Long.compare(s2.getAddTime(), s1.getAddTime());
        if (result != 0)
            return result;
        result = Integer.compare(s2.getLastChapterNum(), s1.getLastChapterNum());
        if (result != 0)
            return result;
        return Integer.compare(s2.getWeight(), s1.getWeight());
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }

    public static class SavedSource {
        String bookName;
        long saveTime;
        BookSourceBean bookSource;

        SavedSource() {
            this.bookName = "";
            saveTime = 0;
        }

        public String getBookName() {
            return this.bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public long getSaveTime() {
            return saveTime;
        }

        public void setSaveTime(long saveTime) {
            this.saveTime = saveTime;
        }

        public BookSourceBean getBookSource() {
            return bookSource;
        }

        public void setBookSource(BookSourceBean bookSource) {
            this.bookSource = bookSource;
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UP_SEARCH_BOOK)})
    public void upSearchBook(SearchBookBean searchBookBean) {
        if (!Objects.equals(book.getBookInfoBean().getName(), searchBookBean.getName())
                || !Objects.equals(book.getBookInfoBean().getAuthor(), searchBookBean.getAuthor())) {
            return;
        }
        for (int i = 0; i < adapter.getSearchBookBeans().size(); i++) {
            if (adapter.getSearchBookBeans().get(i).getTag().equals(searchBookBean.getTag())
                    && !adapter.getSearchBookBeans().get(i).getLastChapter().equals(searchBookBean.getLastChapter())) {
                adapter.getSearchBookBeans().get(i).setLastChapter(searchBookBean.getLastChapter());
                adapter.notifyItemChanged(i);
            }
        }
    }
}
