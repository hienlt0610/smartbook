package smartbook.hutech.edu.smartbook.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import smartbook.hutech.edu.smartbook.common.BaseModel;

/**
 * Created by hienl on 6/24/2017.
 */

public class Page extends BaseModel {
    @SerializedName("pageNumber")
    private int pageNumber;

    @SerializedName("itemList")
    private List<Item> mItemList;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<Item> getItemList() {
        return mItemList;
    }

    public void setItemList(List<Item> itemList) {
        mItemList = itemList;
    }
}
