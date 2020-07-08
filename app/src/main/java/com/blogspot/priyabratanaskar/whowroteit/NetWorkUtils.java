package com.blogspot.priyabratanaskar.whowroteit;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetWorkUtils {
    private static final String LOG_TAG = NetWorkUtils.class.getSimpleName();
    //Base URL for Google books API
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    //Param for search string
    private static final String QUERY_PARAM = "q";
    //Param that limits search result
    private static final String MAX_RESULTS = "maxResults";
    //Param to filter print type
    private static final String PRINT_TYPE = "printType";

    static String getBookInfo(String queryString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;
        try {

            //Create URI using the base url
            Uri builtURI = Uri.parse(BOOK_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryString)
                    .appendQueryParameter(MAX_RESULTS, "10")
                    .appendQueryParameter(PRINT_TYPE, "books")
                    .build();
            //convert URI into URL
            URL requestURL = new URL(builtURI.toString());

            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Get the input sream
            InputStream inputStream = urlConnection.getInputStream();

            //create bufferedReader using inputStream
            reader = new BufferedReader(new InputStreamReader(inputStream));

            //Use string builder to hold responses
            StringBuilder builder = new StringBuilder();

            //It will read from reader and will be used to append with StringBuilder
            String line;

            while ( (line=reader.readLine() ) != null){
                builder.append(line);
                builder.append("\n");
            }
            if (builder.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            bookJSONString = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection!=null){
                urlConnection.disconnect();
            }
            if (reader!=null){
                try {
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        Log.d(LOG_TAG,bookJSONString);
        return bookJSONString;
    }
}
