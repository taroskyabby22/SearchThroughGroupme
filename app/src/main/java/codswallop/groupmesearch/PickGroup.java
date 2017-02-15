package codswallop.groupmesearch;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class PickGroup extends AppCompatActivity {

    String accessToken = "";
    String START = "";
    GroupListAdapter groupListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String MyPREFERENCES = "MyPrefs";
        setContentView(R.layout.activity_pick_group);
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        accessToken = sharedpreferences.getString("ACCESS", "NA");
        this.setTitle("Pick Group to Search");
        if(accessToken.equals( "NA")) {
            Intent intent = getIntent();
            accessToken = intent.getStringExtra("ACCESS");
        }
        START = "https://api.groupme.com/v3/groups?token=" + accessToken;


        groupListAdapter = new GroupListAdapter(this, new ArrayList<PathId>());
        ListView listView = (ListView) findViewById(R.id.listGroups);
        listView.setAdapter(groupListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intents = new Intent(PickGroup.this, SelectedGroupSearch.class);
                intents.putExtra("PathId", groupListAdapter.getId(i));
                intents.putExtra("ACCESS", accessToken);
                startActivity(intents);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        String MyPREFERENCES = "MyPrefs";
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        accessToken = sharedpreferences.getString("ACCESS", "NA");
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.pick_group_menu, menu);
        return true;
    }
    SharedPreferences sharedpreferences;
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        String MyPREFERENCES = "MyPrefs";
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        int id = item.getItemId();

        if(id == R.id.log_out)
        {
            final Dialog dialog = new Dialog(PickGroup.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.logout_popup);
            dialog.show();

            Button yespref = (Button) dialog.findViewById(R.id.saveyes);
            yespref.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("ACCESS", "NA");
                    editor.commit();
                    Intent intent = new Intent(PickGroup.this, LogIn.class);
                    intent.putExtra("ACCESS", "NA");
                    startActivity(intent);
                }
            });

            Button nopref = (Button) dialog.findViewById(R.id.saveno);
            nopref.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class Fetch extends AsyncTask<Void, Void, ArrayList<PathId>> {

        private PathId[] getUrls(String json) throws JSONException {
            final String OWM_LIST = "response";
            JSONObject forecastJson = new JSONObject(json);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            PathId[] rst = new PathId[weatherArray.length()];

            String path;
            String id;
            String name;
            String preview;
            String time;
            for (int i = 0; i < weatherArray.length(); i++) {
                JSONObject dayF = weatherArray.getJSONObject(i);
                path = dayF.getString("image_url");
                id = dayF.getString("group_id");
                name = dayF.getString("name");
                preview = json;
                for (int j = 0; j <= i; j++)
                    preview = preview.substring(preview.indexOf("preview") + 7);
                preview = find("nickname", preview) + ": " + find("text", preview);
                time = json;
                for (int j = 0; j <= i; j++)
                    time = time.substring(time.indexOf("last_message_created_at") + "last_message_created_at".length() + 2);
                time = time.substring(0, time.indexOf(","));
                String mem = json;
                for (int l = 0; l <= i; l++)
                    mem = mem.substring(mem.indexOf("\"members\"") + "\"members\"".length() + 4);
                mem = mem.substring(0, mem.indexOf("}]"));

                int count = (mem.length() - mem.replace("\"user_id\"", "").length()) / "\"user_id\"".length() + 1;
                String[] membersID = new String[count + 1];
                String[] membersNick = new String[count + 1];
                membersID[0] = "";
                membersNick[0] = "All Users";
                for (int k = 1; k <= count ; k++) {
                        mem = mem.substring(mem.indexOf("user_id") + "user_id".length() + 3);
                        membersID[k] = mem.substring(0, mem.indexOf("\""));
                        mem = mem.substring(mem.indexOf("nickname") + "nickname".length() + 3);
                        membersNick[k] = mem.substring(0, mem.indexOf("\""));
                }
                rst[i] = new PathId(path, id, name, preview, time, membersID, membersNick);
            }
            return rst;
        }

        private String find(String what, String json) {
            String lists;
            json = json.substring(json.indexOf(what) + what.length() + 2);
            if (json.substring(0, 1).equals("\""))
                json = json.substring(1, json.length());
            lists = json.substring(0, json.indexOf("\",\""));

            if (lists.contains("\\n")) {
                lists = lists.replace("\\n", "\n");
            }
            if (lists.contains("\\r"))
                lists = lists.replace("\\r", "\r");
            if (lists.contains("\\\""))
                lists = lists.replace("\\\"", "\"");
            return lists;
        }

        /**
         * get info from api
         */
        @Override
        protected ArrayList<PathId> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            //for raw JSON responce
            String forcastJsonStr = null;
            ArrayList<PathId> all = new ArrayList<>();

            START = "https://api.groupme.com/v3/groups?token=" + accessToken;
            int count = 1;
            while (true) {
                try {
                    Uri build;
                    if (count == 1)
                        build = Uri.parse(START);
                    else
                        build = Uri.parse(START + "&page=" + count);
                    count++;
                    URL url = new URL(build.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    //read input
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        Log.e("HERE", "BREAK");
                        break;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\n");
                    }

                    if (buffer.length() == 0) {
                        Log.e("HERE", "BUFFER BREAK");
                        break;
                    }

                    forcastJsonStr = buffer.toString();

                } catch (IOException e) {
                    makeToast();

                    break;
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                        }
                    }
                }

                PathId[] result;
                try {

                    result = getUrls(forcastJsonStr);
                    if (result.length == 0)
                        break;
                    Collections.addAll(all, result);
                } catch (Exception e) {
                    Log.e("NO RESULTS", "BREAK", e);
                    break;
                }
            }

            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("Reader", "Close", e);
                }
            }

            return all;
        }

        @Override
        protected void onPostExecute(ArrayList<PathId> strings) {
            super.onPostExecute(strings);
            if (strings != null) {
                groupListAdapter.clear();
                for (PathId s : strings) {
                    groupListAdapter.add(s);

                }
            }
        }

    }

    public void update() {
        if(isConnectedToInternet())
        {
            Intent intent = getIntent();
            if(accessToken.equals("NA"))
                accessToken = intent.getStringExtra("ACCESS");
            Fetch task = new Fetch();
            task.execute();
        }
        else
        {
            Toast.makeText(this, "PLEASE ENABLE INTERNET ACCESS\nMobile data or WIFI", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
        }
    }

    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    public void makeToast(){


        if(isConnectedToInternet())
        {
            return;
        }
        else
        {
            Toast.makeText(this, "PLEASE ENABLE INTERNET ACCESS\nMobile data or WIFI", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
        }

    }
}
