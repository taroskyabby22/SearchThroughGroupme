package codswallop.groupmesearch;

import java.io.Serializable;

/**
 * Created by Abby on 7/11/2016.
 */
public class PathId implements Serializable {
    String image;
    String id;
    String name;
    String preview;
    String time;
    String[] membersID;
    String[] membersNickname;

    public PathId(String vim, String vid, String vnam, String vprev, String vtime, String[] vmem, String[] vnick) {
        this.image = vim;
        this.id = vid;
        this.name = vnam;
        this.preview = vprev;
        this.time = vtime;
        this.membersID = vmem;
        this.membersNickname = vnick;

    }

    public String getId() {
        return this.id;
    }

}