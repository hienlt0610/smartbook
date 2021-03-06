package smartbook.hutech.edu.smartbook.common.network.builder;
/*
 * Created by Nhat Hoang on 30/06/2017.
 */

import com.google.gson.JsonObject;

import retrofit2.Call;
import smartbook.hutech.edu.smartbook.common.BaseModel;
import smartbook.hutech.edu.smartbook.common.Constant;
import smartbook.hutech.edu.smartbook.common.network.api.BookApi;
import smartbook.hutech.edu.smartbook.model.BookList;
import smartbook.hutech.edu.smartbook.model.BookResponse;
import smartbook.hutech.edu.smartbook.model.CategoryList;

public class ApiClient {
    private ApiRequestListener mListener;

    public ApiClient(ApiRequestListener listener) {
        this.mListener = listener;
    }

    private void requestApi(Class<? extends BaseModel> _class, Call<JsonObject> call) {
        if (mListener != null) {
            mListener.onRequestApi(Constant.CODE_REQUEST_DEFAULT, _class, call);
        }
    }

    public void getCategory() {
        BookApi api = ApiGenerator.getInstance().createService(BookApi.class);
        requestApi(CategoryList.class, api.getCategory());
    }

    public void getListBook(String keyword) {
        BookApi api = ApiGenerator.getInstance().createService(BookApi.class);
        requestApi(BookList.class, api.getListBook(keyword));
    }

    public void getBook(int bookId) {
        BookApi api = ApiGenerator.getInstance().createService(BookApi.class);
        requestApi(BookResponse.class, api.getBook(bookId));
    }
}