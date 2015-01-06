package me.lock8;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Created by joseluisugia on 25/12/14.
 */
public class MzigoUnit implements Callback {

    Semaphore semaphore;
    private final Mzigo client = new Mzigo();

    @Override
    public void onFailure(Request request, IOException e) {
        Assert.assertTrue(false);
        semaphore.release();
    }

    @Override
    public void onResponse(Response response) throws IOException {
        Assert.assertTrue(response.isSuccessful());
        semaphore.release();
    }

    @Before
    public void setUp() {
        client.register(this);
    }

    @Test
    public void testConnection() throws Exception {

        Request request = new Request.Builder().url("http://google.es").build();

        semaphore = new Semaphore(0);

        client.enqueue(request, this);
        semaphore.acquire();
    }

    @After
    public void tearDown() {
        client.unregister(this);
    }
}
