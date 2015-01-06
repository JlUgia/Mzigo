package me.lock8;

import java.io.IOException;
import java.util.LinkedList;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Created by joseluisugia on 25/12/14.
 */
public class ResponseListener implements Callback {

    private final Callback callback;
    private final LinkedList<Object> requestedTags;

    public ResponseListener(Callback callback) {
        this.callback = callback;
        requestedTags = new LinkedList<Object>();
    }

    public void registerTag(Object tag) {
        requestedTags.add(tag);
    }

    public void unregisterTag(Object tag) {
        requestedTags.remove(tag);
    }

    public Callback associatedCallback() {
        return callback;
    }

    public boolean didRequestTag(Object tag) {

        for (Object requestedTag : requestedTags) {
            if (requestedTag.equals(tag)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onFailure(Request request, IOException e) {

        if (didRequestTag(request.tag())) {
            callback.onFailure(request, e);
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {

        if (didRequestTag(response.request().tag())) {
            callback.onResponse(response);
        }
    }
}
