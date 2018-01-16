package co.melondev.cubedpay.api;

import co.melondev.cubedpay.Callback;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class UrlRequests {

    /**
     * Get response from a url.
     *
     * @param url the url
     * @param response the return data in form of a callback.
     */
    public void get(String url, Callback<UrlResponse> response) {
        new Thread(() -> request(url, null, ResponseType.GET, response)).start();
    }

    /**
     * Sends a put response to a url with the certain data.
     *
     * @param url  the url
     * @param data the data
     * @param response the return data in form of a callback.
     */
    public void put(String url, Object data, Callback<UrlResponse> response) {
        new Thread(() -> request(url, data, ResponseType.PUT, response)).start();
    }

    /**
     * Sends a post response to a url with the certain data.
     *
     * @param url  the url
     * @param data the data
     * @param response the return data in form of a callback.
     */
    public void post(String url, Object data, Callback<UrlResponse> response) {
        new Thread(() -> request(url, data, ResponseType.POST, response)).start();
    }

    /**
     * Request a response from a url with a certain type.
     *
     * @param url  the url
     * @param data the data
     * @param response the return data in form of a callback.
     */
    private void request(String url, Object data, ResponseType type, Callback<UrlResponse> response) {
        UrlResponse urlResponse = new UrlResponse();
        try {
            URL urlObject = new URL(url);
            // Create connection
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            // Set user agent to chrome just so nothing funky happens.
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            connection.setRequestMethod(type.name());
            // Set properties to be json
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            // Write and send if not a get
            if (type != ResponseType.GET && data != null) {
                connection.setDoOutput(true);
                OutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(writeValueAsBytes(data));
                outputStream.flush();
                outputStream.close();
            }
            // Output all the required info.
            try {
                urlResponse.setResponseCode(connection.getResponseCode());
                urlResponse.setResponseMessage(streamToString(connection.getInputStream()));
            } catch (FileNotFoundException e) {
                // We probably 404ed because java is horrible and throws this exception on a 404.
                urlResponse.setResponseCode(404);
                urlResponse.setResponseMessage(streamToString(connection.getErrorStream()));
                urlResponse.setExceptionMessage(getStackTrace(e));
            }
            connection.disconnect();
        } catch (IOException e) {
            // A not so happy exception occurred here. Could be a load of things because java loves exceptions.
            urlResponse.setResponseCode(404);
            urlResponse.setExceptionMessage(getStackTrace(e));
        }
        response.callback(urlResponse);
    }

    /**
     * Will convert a object to a byte array.
     *
     * @param object the object
     * @return a byte array of the object.
     */
    private byte[] writeValueAsBytes(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        } catch (IOException ignored) {
        }
        return new byte[]{};
    }

    /**
     * Will convert a input stream into a string format.
     *
     * @param stream the stream
     * @return a string readable version of the input stream.
     */
    private String streamToString(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
    }

    /**
     * Will convert a throwable/exception into a string format.
     *
     * @param throwable the throwable/exception
     * @return a string readable version of the stack trace.
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private enum ResponseType {
        /**
         * Get response type.
         */
        GET,
        /**
         * Put response type.
         */
        PUT,
        /**
         * Post response type.
         */
        POST
    }
}
