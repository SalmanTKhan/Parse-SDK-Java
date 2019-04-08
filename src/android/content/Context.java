package android.content;

import java.io.File;

public class Context {

    Context applicationContext;

    public Context() {
        File cacheDir = new File(getCacheDir());
        cacheDir.mkdirs();
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public String getCacheDir() {
        return "Parse/Cache";
    }
}
