package com.virmana.flix.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Source implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("quality")
    @Expose
    private String quality;

    @SerializedName("size")
    @Expose
    private String size;

    @SerializedName("kind")
    @Expose
    private String kind;

    @SerializedName("premium")
    @Expose
    private String premium;

    @SerializedName("external")
    @Expose
    private Boolean external;

    @SerializedName("url")
    @Expose
    private String url;


    protected Source(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        type = in.readString();
        title = in.readString();
        quality = in.readString();
        size = in.readString();
        kind = in.readString();
        premium = in.readString();
        byte tmpExternal = in.readByte();
        external = tmpExternal == 0 ? null : tmpExternal == 1;
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(quality);
        dest.writeString(size);
        dest.writeString(kind);
        dest.writeString(premium);
        dest.writeByte((byte) (external == null ? 0 : external ? 1 : 2));
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Source> CREATOR = new Creator<Source>() {
        @Override
        public Source createFromParcel(Parcel in) {
            return new Source(in);
        }

        @Override
        public Source[] newArray(int size) {
            return new Source[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setPremium(String premium) {
        this.premium = premium;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public String getTitle() {
        return title;
    }

    public String getQuality() {
        return quality;
    }

    public String getSize() {
        return size;
    }

    public String getKind() {
        return kind;
    }

    public String getPremium() {
        return premium;
    }

    public Boolean getExternal() {
        return external;
    }
}
