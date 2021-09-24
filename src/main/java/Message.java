/**
 * TODO
 *
 * @author <a href="mailto:meilinsenchn@gmail.com">meils</a>
 * @date 2021/9/22 6:15 下午
 * @since
 */
public class Message {

    private int streamId;
    private String message;

    Message(){

    }

    Message(int streamId, String message){
        this.streamId = streamId;
        this.message = message;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
