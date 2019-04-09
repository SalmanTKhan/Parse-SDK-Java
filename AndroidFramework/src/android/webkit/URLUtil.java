package android.webkit;

public class URLUtil {

    // to refer to bar.png under your package's asset/foo/ directory, use
    // "file:///android_asset/foo/bar.png".
    static final String ASSET_BASE = "file:///android_asset/";
    // to refer to bar.png under your package's res/drawable/ directory, use
    // "file:///android_res/drawable/bar.png". Use "drawable" to refer to
    // "drawable-hdpi" directory as well.
    static final String RESOURCE_BASE = "file:///android_res/";
    static final String FILE_BASE = "file://";
    static final String PROXY_BASE = "file:///cookieless_proxy/";
    static final String CONTENT_BASE = "content:";

    /**
     * @return True iff the url is valid.
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }

        return (isAssetUrl(url) ||
                isResourceUrl(url) ||
                isFileUrl(url) ||
                isAboutUrl(url) ||
                isHttpUrl(url) ||
                isHttpsUrl(url) ||
                isJavaScriptUrl(url) ||
                isContentUrl(url));
    }

    /**
     * @return True iff the url is an asset file.
     */
    public static boolean isAssetUrl(String url) {
        return (null != url) && url.startsWith(ASSET_BASE);
    }

    /**
     * @return True iff the url is a resource file.
     * @hide
     */
    public static boolean isResourceUrl(String url) {
        return (null != url) && url.startsWith(RESOURCE_BASE);
    }

    /**
     * @return True iff the url is a proxy url to allow cookieless network
     * requests from a file url.
     * @deprecated Cookieless proxy is no longer supported.
     */
    @Deprecated
    public static boolean isCookielessProxyUrl(String url) {
        return (null != url) && url.startsWith(PROXY_BASE);
    }

    /**
     * @return True iff the url is a local file.
     */
    public static boolean isFileUrl(String url) {
        return (null != url) && (url.startsWith(FILE_BASE) &&
                !url.startsWith(ASSET_BASE) &&
                !url.startsWith(PROXY_BASE));
    }

    /**
     * @return True iff the url is an about: url.
     */
    public static boolean isAboutUrl(String url) {
        return (null != url) && url.startsWith("about:");
    }

    /**
     * @return True iff the url is a data: url.
     */
    public static boolean isDataUrl(String url) {
        return (null != url) && url.startsWith("data:");
    }

    /**
     * @return True iff the url is a javascript: url.
     */
    public static boolean isJavaScriptUrl(String url) {
        return (null != url) && url.startsWith("javascript:");
    }

    /**
     * @return True iff the url is an http: url.
     */
    public static boolean isHttpUrl(String url) {
        return (null != url) &&
                (url.length() > 6) &&
                url.substring(0, 7).equalsIgnoreCase("http://");
    }

    /**
     * @return True iff the url is an https: url.
     */
    public static boolean isHttpsUrl(String url) {
        return (null != url) &&
                (url.length() > 7) &&
                url.substring(0, 8).equalsIgnoreCase("https://");
    }

    /**
     * @return True iff the url is a network url.
     */
    public static boolean isNetworkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return isHttpUrl(url) || isHttpsUrl(url);
    }

    /**
     * @return True iff the url is a content: url.
     */
    public static boolean isContentUrl(String url) {
        return (null != url) && url.startsWith(CONTENT_BASE);
    }

}
