package com.blogspot.priyabratanaskar.whowroteit;

import android.content.Context;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    // Variables for the search input field and results TextViews.
    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;
    private TextInputLayout bookInputLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookInputLayout = findViewById(R.id.book_input_layout);
        mBookInput = findViewById(R.id.bookInput);
        mTitleText = findViewById(R.id.titleText);
        mAuthorText = findViewById(R.id.authorText);
    }
    public void searchBooks(View view){
        // Get the search string from the input field.
        String queryString = mBookInput.getText().toString();
        //Handle search book operation

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
        Toast.makeText(this, "Searching Query Over Internet",Toast.LENGTH_LONG).show();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()
                && queryString.length() != 0) {
            new FetchBook(mTitleText,mAuthorText).execute(queryString);
            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);
        } else {
            if (queryString.length() == 0) {
                mAuthorText.setText("");
                mTitleText.setText("");
                bookInputLayout.setError(getString(R.string.no_search_term));
            } else {
                mAuthorText.setText("");
                mTitleText.setText("");
                bookInputLayout.setError(getString(R.string.no_network));
            }
        }
    }
    private class FetchBook extends AsyncTask<String,Void,String>{
        private WeakReference<TextView> mTitleText;
        private WeakReference<TextView> mAuthorText;

        @Override
        protected String doInBackground(String... strings) {
            Log.d("din back",strings[0]);
            return NetWorkUtils.getBookInfo(strings[0]);
        }

        public FetchBook(TextView mTitleText, TextView mAuthorText) {
            this.mTitleText = new WeakReference<>(mTitleText);
            this.mAuthorText = new WeakReference<>(mAuthorText);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                //Convert response into JSONObject
                JSONObject jsonObject = new JSONObject(s);

                //Get book-item array from jsonObject
                JSONArray jsonArray = jsonObject.getJSONArray("items");

                //Use index to work with jsonArray
                int index = 0;
                String title = null;
                String authors = null;

                while (index < jsonArray.length() && (title == null && authors == null )){
                    // Get the current item information.
                    JSONObject book = jsonArray.getJSONObject(index);
                    JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                    // Try to get the author and title from the current item,
                    // catch if either field is empty and move on.
                    try {
                        title = volumeInfo.getString("title");

                        JSONArray authorArray = volumeInfo.getJSONArray("authors");

                        int authorIndex =0;
                        while (authorIndex < authorArray.length()){
                            if(authors==null){
                                authors = authorArray.getString(authorIndex);
                            }else {
                                authors = authors + "\n" + authorArray.getString(authorIndex);
                            }
                            authorIndex++;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    index++;
                }

                if (title != null && authors != null) {
                    mTitleText.get().setText(title);
                    mAuthorText.get().setText(authors);
                } else {
                    // If both are null, update the UI to show failed results.
                    mTitleText.get().setText(R.string.no_results);
                    mAuthorText.get().setText("");
                }

            }catch (JSONException e){
                /**
                 * If onPostExecute does not receive a proper JSON string, update the UI to show failed results.
                 */
                mTitleText.get().setText(R.string.no_results);
                mAuthorText.get().setText("");
            }
        }
    }
}
