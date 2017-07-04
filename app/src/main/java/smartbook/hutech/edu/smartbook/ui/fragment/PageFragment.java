package smartbook.hutech.edu.smartbook.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import smartbook.hutech.edu.smartbook.R;
import smartbook.hutech.edu.smartbook.common.App;
import smartbook.hutech.edu.smartbook.common.BaseFragment;
import smartbook.hutech.edu.smartbook.common.Constant;
import smartbook.hutech.edu.smartbook.common.interfaces.IBookViewAction;
import smartbook.hutech.edu.smartbook.common.interfaces.ISaveHighlight;
import smartbook.hutech.edu.smartbook.common.interfaces.ISwipePage;
import smartbook.hutech.edu.smartbook.common.view.bookview.BookImageView;
import smartbook.hutech.edu.smartbook.database.Answered;
import smartbook.hutech.edu.smartbook.database.AnsweredDao;
import smartbook.hutech.edu.smartbook.model.bookviewer.BookItemModel;
import smartbook.hutech.edu.smartbook.model.bookviewer.BookPageModel;
import smartbook.hutech.edu.smartbook.utils.FileUtils;
import smartbook.hutech.edu.smartbook.utils.StringUtils;

/**
 * Created by hienl on 6/24/2017.
 */
public class PageFragment extends BaseFragment implements IBookViewAction {
    private static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";
    private static final String EXTRA_PAGE = "EXTRA_PAGE";
    private static final String EXTRA_ROOT_FOLDER = "EXTRA_ROOT_FOLDER";
    @BindView(R.id.fragmentPage_imgBook)
    BookImageView mBookImageView;
    private BookPageModel mPage;
    private String mBookFolder;
    private String mBookId;
    private AnsweredDao mAnsweredDao;

    public static PageFragment newInstance(BookPageModel page, String bookId, String bookFolder) {
        Bundle args = new Bundle();
        String jsonPage = new Gson().toJson(page);
        args.putString(EXTRA_PAGE, jsonPage);
        args.putString(EXTRA_BOOK_ID, bookId);
        args.putString(EXTRA_ROOT_FOLDER, bookFolder);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getResId() {
        return R.layout.fragment_page;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Init data
        Bundle bundle = getArguments();
        boolean hasPageData = bundle != null && bundle.containsKey(EXTRA_PAGE);
        if (hasPageData) {
            String string = bundle.getString(EXTRA_PAGE);
            mPage = new Gson().fromJson(string, BookPageModel.class);
        }

        boolean hasRootFolder = bundle != null && bundle.containsKey(EXTRA_ROOT_FOLDER);
        if (hasRootFolder) {
            mBookFolder = bundle.getString(EXTRA_ROOT_FOLDER);
        }

        boolean hasBookInfo = bundle != null && bundle.containsKey(EXTRA_BOOK_ID);
        if (hasBookInfo) {
            String string = bundle.getString(EXTRA_PAGE);
            mBookId = bundle.getString(EXTRA_BOOK_ID);
        }

        //Init dao
        mAnsweredDao = App.getApp().getDaoSession().getAnsweredDao();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImage();
        loadListItem();
        loadListAnswer();

        //Set listener
        mBookImageView.setBookActionListener(this);
    }

    private void loadListAnswer() {
        if (mBookId != null && mPage != null) {
            String bookId = String.valueOf(mBookId);
            List<Answered> listAnswer = mAnsweredDao.queryBuilder().where(AnsweredDao.Properties.Bid.eq(bookId),
                    AnsweredDao.Properties.Page.eq(mPage.getPageIndex())).list();
            Map<Integer, String> mapAnswer = new HashMap<>();
            for (Answered answered : listAnswer) {
                mapAnswer.put(answered.getQuizIndex(), answered.getQuizAnswer());
            }
            mBookImageView.setAnswers(mapAnswer);
        }
    }

    private void loadListItem() {
        mBookImageView.setListBookItem(mPage.getItems());
    }

    public void loadImage() {
        File bookFile = FileUtils.getFileByPath(mBookFolder);
        File imageFile = FileUtils.separatorWith(bookFile, mPage.getImagePath());

        String fileName = StringUtils.leftPad(String.valueOf(mPage.getPageIndex()), 3, '0') + ".png";
        File highlightFolder = FileUtils.separatorWith(bookFile, Constant.HIGHLIGHT_FOLDER_NAME);
        File hightlightImageFile = FileUtils.separatorWith(highlightFolder, fileName);
        if (FileUtils.isFileExists(hightlightImageFile)) {
            Glide.with(this).load(hightlightImageFile).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mBookImageView.setBitmapHighlight(resource);
                }
            });
        }

        Glide.with(this).load(imageFile)
                .asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mBookImageView.setImageBitmap(resource);
            }
        });
    }

    /**
     * Check if Image is Zooming
     *
     * @return
     */
    public boolean isZoomed() {
        if (mBookImageView != null) {
            return mBookImageView.isZoomed();
        }
        return false;
    }

    /**
     * Zoom image to normal
     */
    public void zoomToNormal() {
        if (mBookImageView != null) {
            mBookImageView.resetZoom();
        }
    }

    public boolean isHighlight() {
        if (mBookImageView != null) {
            return mBookImageView.isHighlightMode();
        }
        return false;
    }

    public BookImageView getBookImageView() {
        return mBookImageView;
    }

    @Override
    public void onInputClick(final int position) {
        BookItemModel item = mPage.getItems().get(position);
        String answer = mBookImageView.getItemResult(position);
        new MaterialDialog.Builder(getActivity())
                .title("Nhập câu trả lời")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(null, answer, true, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        mBookImageView.setItemResult(input.toString(), position);
                        if (mBookId != null) {
                            String bookId = String.valueOf(mBookId);
                            Answered answered = new Answered(null, mBookId, mPage.getPageIndex(), position, input.toString());
                            mAnsweredDao.insertOrReplace(answered);
                        }
                    }
                }).show();
    }

    @Override
    public void onAudioClick(int position) {
        BookItemModel item = mPage.getItems().get(position);
        Toast.makeText(getContext(), "Play audio: " + item.getData().get(0), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectBoxClick(final int position) {
        BookItemModel item = mPage.getItems().get(position);
        new MaterialDialog.Builder(getActivity())
                .title("Chọn câu trả lời đúng")
                .items(item.getData())
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int pos, CharSequence text) {
                        mBookImageView.setItemResult(text.toString(), position);
                        if (mBookId != null) {
                            String bookId = String.valueOf(mBookId);
                            Answered answered = new Answered(null, bookId, mPage.getPageIndex(),
                                    position, text.toString());
                            mAnsweredDao.insertOrReplace(answered);
                        }
                    }
                }).show();
    }

    @Override
    public void onNavigationClick(int position) {
        BookItemModel item = mPage.getItems().get(position);
        List<Object> data = item.getData();
        if (data.size() > 0) {
            int page = (int) Math.round((double) data.get(0));
            if (getActivity() instanceof ISwipePage) {
                ((ISwipePage) getActivity()).swipePage(page);
            }
        }
    }
//
//    @Override
//    public void onHighlightDrawed() {
//        if (getActivity() instanceof ISaveHighlight) {
//            Bitmap bitmap = mBookImageView.getHighlightBitmap();
//            ((ISaveHighlight) getActivity()).saveCurrentHighlight(bitmap);
//        }
//    }


    @Override
    public void onStop() {
        super.onStop();
        if (mBookImageView.isHighlightSaveAble()) {
            if (getActivity() instanceof ISaveHighlight) {
                Bitmap bitmap = mBookImageView.getHighlightBitmap();
                ((ISaveHighlight) getActivity()).saveCurrentHighlight(mPage.getPageIndex(), bitmap);
            }
        }
    }
}
