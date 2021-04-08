package com.example.tabnote;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerMessages {
    private final String serverAddress = "https://tabnote.herokuapp.com/tabs";

    public void showUsersTabs(UsersTabsAdapter usersTabsAdapter, ProgressBar progressBar) {
        ServerAsyncTask serverAsyncTask = new ServerAsyncTask("showAll", usersTabsAdapter, progressBar);
        serverAsyncTask.execute();
    }

    public void getAll(ArrayList<Tab> tabArrayList) {
        ServerAsyncTask serverAsyncTask = new ServerAsyncTask("getAll", tabArrayList);
        serverAsyncTask.execute();
    }

    public void addTab(Tab tab, Context context) {
        ServerAsyncTask serverAsyncTask = new ServerAsyncTask("addTab", tab, context);
        serverAsyncTask.execute();
    }

    public void deleteTab(Tab tab, Context context) {
        ServerAsyncTask serverAsyncTask = new ServerAsyncTask("deleteTab", tab, context);
        serverAsyncTask.execute();
    }

    class ServerAsyncTask extends AsyncTask {
        private String rez;
        private final String responseType;
        Context context;
        UsersTabsAdapter usersTabsAdapter;
        ProgressBar progressBar;
        ArrayList<Tab> tabs;
        Tab tab;

        ServerAsyncTask(String responseType, UsersTabsAdapter usersTabsAdapter, ProgressBar progressBar) {
            this.responseType = responseType;
            this.usersTabsAdapter = usersTabsAdapter;
            this.progressBar = progressBar;
        }

        ServerAsyncTask(String responseType, ArrayList<Tab> tabs) {
            this.responseType = responseType;
            this.tabs = tabs;
        }

        ServerAsyncTask(String responseType, Tab tab, Context context) {
            this.responseType = responseType;
            this.tab = tab;
            this.context = context;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient httpclient = new DefaultHttpClient();
            rez = "";
            try {
                switch (responseType) {
                    case "showAll":
                        progressBar.setVisibility(ProgressBar.VISIBLE);

                    case "getAll":
                        HttpGet httpGet = new HttpGet(serverAddress);
                        HttpResponse response = httpclient.execute(httpGet);
                        HttpEntity entity = response.getEntity();
                        rez += EntityUtils.toString(entity);
                        break;

                    case "addTab":
                        HttpPost httppost = new HttpPost(serverAddress + "/add");
                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("userName", tab.userName));
                        nameValuePairs.add(new BasicNameValuePair("title", tab.title));
                        nameValuePairs.add(new BasicNameValuePair("body", tab.body));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                        // Выполняем HTTP Post запрос
                        HttpResponse resp= httpclient.execute(httppost);
                        if (resp.getStatusLine().getStatusCode() == 200) {
//                            Toast.makeText(context, "Ваша табулатура опубликована", Toast.LENGTH_LONG).show();
                        } else {
//                            Toast.makeText(context, "Ваша табулатура не опубликована", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case "deleteTab":
                        HttpPost http = new HttpPost(serverAddress + "/remove");
                        List<NameValuePair> values = new ArrayList<>();
                        values.add(new BasicNameValuePair("id", tab.id+ ""));
                        http.setEntity(new UrlEncodedFormEntity(values, "UTF-8"));
                        // Выполняем HTTP Post запрос
                        HttpResponse res= httpclient.execute(http);
                        if (res.getStatusLine().getStatusCode() == 200) {
//                            Toast.makeText(context, "Ваша табулатура удалена", Toast.LENGTH_LONG).show();
                        } else {
//                            Toast.makeText(context, "Ваша табулатура не удалена", Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                switch (responseType) {
                    case "showAll":
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        ArrayList<Tab> tabArrayList = readJSON(rez);
                        usersTabsAdapter.swap(tabArrayList);
                        break;
                    case "getAll":
                        tabs.clear();
                        tabs.addAll(readJSON(rez));
                        break;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<Tab> readJSON(String jsonText) throws IOException, JSONException {
            ArrayList<Tab> tabArrayList = new ArrayList<>();

            String crappyPrefix = "null";

            if(jsonText.startsWith(crappyPrefix)){
                jsonText = jsonText.substring(crappyPrefix.length());
            }

            // создаём json объект
            JSONObject jsonObject = new JSONObject(jsonText);

            for (int i = 0; i < jsonObject.length(); i++) {
                JSONObject tabObj = jsonObject.getJSONObject(String.valueOf(i));

                int id = tabObj.getInt("id");
                String userName = tabObj.getString("userName");
                String title = tabObj.getString("title");
                String body = tabObj.getString("body");

                Tab tab = new Tab(id, userName, title, body);
                tabArrayList.add(tab);
            }
            return tabArrayList;
        }
    }
}
