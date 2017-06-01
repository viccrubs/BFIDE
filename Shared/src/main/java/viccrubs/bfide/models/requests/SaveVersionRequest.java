package viccrubs.bfide.models.requests;

import com.google.gson.annotations.Expose;

/**
 * Created by viccrubs on 2017/6/1.
 */
public class SaveVersionRequest extends Request {
    @Expose
    public final String content;
    @Expose
    public final long timestamp;
    @Expose
    public final String projectName;


    public SaveVersionRequest(String content, long timestamp, String projectName) {
        this.content = content;
        this.timestamp = timestamp;
        this.projectName = projectName;
    }
}
