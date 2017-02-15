package codswallop.groupmesearch;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchInfo implements Parcelable{
    String avatar_url;
    String created_at;
    String favorited_by;
    String group_id;
    String id;
    String name;
    String sender_id;
    String sender_type;
    String source_guid;
    String system;
    String text;
    String user_id;
    String attach;
    String preview_attach;
    String selected;

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(avatar_url);
        out.writeString(created_at);
        out.writeString(favorited_by);
        out.writeString(group_id);
        out.writeString(id);
        out.writeString(name);
        out.writeString(sender_id);
        out.writeString(sender_type);
        out.writeString(source_guid);
        out.writeString(system);
        out.writeString(text);
        out.writeString(user_id);
        out.writeString(attach);
        out.writeString(preview_attach);
        out.writeString(selected);
    }

    public int describeContents() {
        return 0;
    }

    public SearchInfo(String avatar_url, String created_at, String favorited_by,
                      String group_id, String id, String name,
                      String sender_id, String sender_type, String source_guid,
                      String system, String text, String user_id, String attach,
                      String preview_attach, String selected) {
        this.avatar_url = avatar_url;
        this.created_at = created_at;
        this.favorited_by = favorited_by;
        this.group_id = group_id;
        this.id = id;
        this.name = name;
        this.sender_id = sender_id;
        this.sender_type = sender_type;
        this.source_guid = source_guid;
        this.system = system;
        this.text = text;
        this.user_id = user_id;
        this.attach = attach;
        this.preview_attach = preview_attach;
        this.selected = selected;

    }
    private SearchInfo(Parcel in)
    {
        avatar_url = in.readString();
        created_at = in.readString();
        favorited_by = in.readString();
        group_id = in.readString();
        id = in.readString();
        name = in.readString();
        sender_id = in.readString();
        sender_type = in.readString();
        source_guid = in.readString();
        system = in.readString();
        text = in.readString();
        user_id = in.readString();
        attach = in.readString();
        preview_attach = in.readString();
        selected = in.readString();
    }

    public static final Parcelable.Creator<SearchInfo> CREATOR = new Parcelable.Creator<SearchInfo>() {
        public SearchInfo createFromParcel(Parcel in) {
            return new SearchInfo(in);
        }

        public SearchInfo[] newArray(int size) {
            return new SearchInfo[size];
        }
    };

}