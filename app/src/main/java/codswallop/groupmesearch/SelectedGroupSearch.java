package codswallop.groupmesearch;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


//SEARCH WITHIN THE CHOOSEN GROUP
public class SelectedGroupSearch extends AppCompatActivity {

    PathId GROUP = null;
    ArrayAdapter<String> adapter;
    ProgressBar firstBar;
    Spinner spinYear;
    String sender_id_find = "";
    String START = "https://api.groupme.com/v3/groups/";
    String AFTERID = "/messages?token=";
    String LAST = "&limit=100";
    SearchAdapter searchAdapter;
    TextView results;
    Fetch task = new Fetch();
    long dateFrom;
    long dateTo;
    ArrayList<SearchInfo> searchAdapterSave;
    ListView messages;
    String accessToken;
    String userMessage = "";
    boolean searchComplete;


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        GROUP = (PathId) intent.getSerializableExtra("PathId");
        searchAdapterSave = intent.getParcelableArrayListExtra("key");
        accessToken = intent.getStringExtra("ACCESS");
        if (firstBar.getProgress() == firstBar.getMax())
            firstBar.getLayoutParams().height = 0;
    }

    @Override
    public void onBackPressed() {
        task.cancel(true);
        Intent intent = new Intent();
        intent.putExtra("ACCESS", GROUP.id);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        firstBar = (ProgressBar) findViewById(R.id.firstBar);
        results = (TextView) findViewById(R.id.results);
        if (GROUP == null) {
            GROUP = (PathId) getIntent().getSerializableExtra("PathId");
            this.setTitle(GROUP.name);
        }

        adapter = new ArrayAdapter<>(SelectedGroupSearch.this, R.layout.spinner_item, GROUP.membersNickname);


        spinYear = (Spinner) findViewById(R.id.person);
        spinYear.setAdapter(adapter);
        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sender_id_find = GROUP.membersID[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (savedInstanceState == null || !savedInstanceState.containsKey("key")) {
            searchAdapter = new SearchAdapter(this, new ArrayList<SearchInfo>());
        } else {
            searchAdapterSave = savedInstanceState.getParcelableArrayList("key");
            searchAdapter = new SearchAdapter(this, searchAdapterSave);
        }
        messages = (ListView) findViewById(R.id.messageShow);

        messages.setAdapter(searchAdapter);
        messages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    searchAdapter.getId(i).selected = "true";
                    String messageID = searchAdapter.getId(i).id;
                    Intent intent = new Intent(SelectedGroupSearch.this, Choice.class);
                    intent.putExtra("BEFOREID", messageID);
                    intent.putExtra("GROUP", GROUP);
                    intent.putExtra("ACCESS", accessToken);
                    task.cancel(true);
                    if(!searchComplete)
                        results.setText(getResources().getText(R.string.cancel));
                    startActivity(intent);

            }
        });
        Button go = (Button) findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean go = true;
                EditText message = (EditText) findViewById(R.id.keyword);
                userMessage = message.getText().toString();
                EditText to = (EditText) findViewById(R.id.dateTo);
                if (to.getText().toString().length() > 0) {
                    try {
                        int upOne = Integer.parseInt(to.getText().toString().substring(3, 5));
                        upOne++;
                        String convert = "" + upOne;
                        if ((convert).length() == 1)
                            convert = "0" + convert;
                        convert = to.getText().toString().substring(0, 2) + "/" + convert + "/" + to.getText().toString().substring(6, 10);
                        dateTo = convert(convert);
                    } catch (Exception e) {
                        go = false;
                    }
                }
                EditText from = (EditText) findViewById(R.id.dateFrom);
                dateFrom = convert(from.getText().toString());
                if (dateTo < dateFrom && dateTo != 0) {
                    Long temp = dateTo;
                    dateTo = dateFrom;
                    dateFrom = temp;
                }
                if (dateTo == -1 || dateFrom == -1)
                    go = false;

                if (go)
                    update();
                else
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.date_wrong), Toast.LENGTH_LONG).show();
            }
        });

    }

    public long convert(String dates) {

        if (!dates.equals("")) {
            if(dates.length()!=10)
                return -1;
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date date;
            try {
                date = formatter.parse(dates);
                long output = date.getTime() / 1000L;
                String str = Long.toString(output);
                return Long.parseLong(str);
            } catch (Exception e) {
                return -1;
            }
        }
        return 0;
    }


    String beforeId = "";

    String created_at = "";


    public class Fetch extends AsyncTask<Void, Integer, ArrayList<SearchInfo>> {

        private ArrayList<SearchInfo> getUrls(String json, ArrayList<SearchInfo> all) throws JSONException {
            String currentItem;
            String itemNoAt;

            //for bar
            int count = Integer.parseInt((json.substring(json.indexOf("\"count\"") + "\"count\"".length() + 1))
                    .substring(0, (json.substring(json.indexOf("\"count\"") + "\"count\"".length() + 1)).indexOf(",")));
            firstBar.setMax(count);

            json = json.substring(json.indexOf("messages\":[") + "messages\":[".length());
            int numthrough = (json.length() - json.replace("},{\"atta", "").length()) / "},{\"atta".length() + 1;
            String avatar = "";
            String name = "";
            String text = "";
            String attach = "";
            String preview_attach = "";
            for (int j = 0; j < numthrough; j++) {
                //GET THE ONE TO PARSE
                if (json.contains("},{\"atta")) {
                    currentItem = json.substring(0, json.indexOf("},{\"atta") + 1);
                    json = json.substring(json.indexOf("},{\"atta") + 2);
                } else {
                    currentItem = json.substring(0, json.indexOf("}]}") + 1);
                    json = "";
                }

                //DELETE IN ATTACHMENTS
                int at = 0;
                itemNoAt = currentItem.substring(currentItem.indexOf("["));
                for (int i = 0; i < itemNoAt.length(); i++) {
                    char charc = itemNoAt.charAt(i);
                    if (charc == '[')
                        at++;
                    if (charc == ']')
                        at--;
                    if (at == 0) {
                        itemNoAt = itemNoAt.substring(0, i + 1);
                        break;
                    }
                }

                if (itemNoAt.contains("\"image\"")) {
                    attach = find("\"url\":", itemNoAt, "}");
                } else if (itemNoAt.contains("\"video\"")) {
                    attach = find("\"preview_attach\":", itemNoAt, "\"type\":");
                    preview_attach = find("\"url\":", itemNoAt, "}");
                }

                currentItem = currentItem.replace(itemNoAt, "[]");

                String sender_id = find("\"sender_id\":", currentItem, "\"sender_type\":");
                if (sender_id.equals(sender_id_find) || sender_id_find.equals("")) {
                    avatar = find("\"avatar_url\":", currentItem, "\"created_at\":");
                    created_at = find("\"created_at\":", currentItem, "\"favorited_by\":");
                    //String favorite = find("\"favorited_by\":", currentItem, "\"group_id\":");
                    //String group_id = find("\"group_id\":", currentItem, "\"id\":");
                    name = find("\"name\":", currentItem, "\"sender_id\":");
                    //String sender_type = find("\"sender_type\":", currentItem, "\"source_guid\":");
                    //String sender_guid = find("\"source_guid\":", currentItem, "\"system\":");
                    //String system = find("\"system\":", currentItem, "\"text\":");
                    text = find("\"text\":", currentItem, "\"user_id\":");
                    //String user_id = find("\"user_id\":", currentItem, "}");


                }
                String id = find("\"id\":", currentItem, "\"name\":");
                if (!preview_attach.equals("")) {
                    text = text + "\n\nVIDEO AVAILABLE";
                } else if (!attach.equals("")) {
                    text = text + "\n\nIMAGE AVAILABLE";
                }
                beforeId = id;

                if ((sender_id.equals(sender_id_find) || sender_id_find.equals(""))
                        &&
                        (text.toLowerCase().contains(userMessage.toLowerCase()) || userMessage.equals(""))
                        &&
                        (Long.parseLong(created_at) <= dateTo || dateTo == 0)
                        &&
                        (Long.parseLong(created_at) >= dateFrom || dateFrom == 0)
                        ) {

                    all.add(new SearchInfo(avatar, created_at, "", "", id, name, sender_id, "", "", "", text, "", attach, preview_attach, ""));
                }
                avatar = "";
               // id = "";
                //sender_id = "";
                text = "";
                //attach = "";
                preview_attach = "";
                attach = "";
            }

            return all;
        }

        public String find(String what, String json, String next) {
            json = json.substring(json.indexOf(what) + what.length());
            json = json.substring(0, json.indexOf(next) - 1);
            if (json.equals("null"))
                json = "";
            if((json.equals("")))
                json = "";
            else {
                if (json.charAt(0) == '"') {
                    json = json.substring(1);
                }
                if (json.charAt(json.length() - 1) == '"')
                    json = json.substring(0, json.length() - 1);
            }
            json = json.replace("\\\"", "\"");
            json = json.replace("\\n", "\n");
            return json;
        }



        /**
         * get info from api
         */
        ArrayList<SearchInfo> all = new ArrayList<>();
        int count = 0;

        @Override
        protected ArrayList<SearchInfo> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //for raw JSON responce
            String forcastJsonStr = null;

            while (true) {
                try {
                    if(isCancelled())
                        break;
                    Uri build;
                    if (beforeId.equals("")) {
                        build = Uri.parse(START + GROUP.id + AFTERID + accessToken + LAST);
                    } else {
                        build = Uri.parse(START + GROUP.id + AFTERID + accessToken + LAST+ "&before_id=" + beforeId);
                    }
                    URL url = new URL(build.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    count++;


                    //read input
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        Log.e("Error", "No InputStream");
                        break;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\n");
                    }

                    if (buffer.length() == 0) {
                        Log.e("Error", "No Buffer Length");
                        break;
                    }

                    forcastJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e("Error", "IOException", e);
                    break;
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("SEARCH", "READER", e);
                        }
                    }
                }


                if (
                        (forcastJsonStr.contains("\"sender_id\":\"" + sender_id_find) || sender_id_find.equals(""))
                                &&
                                (forcastJsonStr.toLowerCase().contains(userMessage.toLowerCase()) || userMessage.equals("") || userMessage.toLowerCase().contains("video") || userMessage.toLowerCase().contains("image"))
                        ) {
                    try {
                        all = getUrls(forcastJsonStr, all);
                        if (Long.parseLong(created_at) < dateFrom) {
                            Log.e("Error", "Date");
                            break;
                        }
                    } catch (Exception e) {
                        Log.e("Error", "Exception", e);
                        break;
                    }
                } else {
                    while (forcastJsonStr.contains("\"id\":")) {
                        forcastJsonStr = forcastJsonStr.substring(forcastJsonStr.indexOf("\"id\":") + ("\"id\":").length() + 1);
                    }
                    beforeId = forcastJsonStr.substring(0, forcastJsonStr.indexOf("\""));
                }
                publishProgress();
            }

            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("SEARCH", "READER", e);
                }
            }

            return all;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            firstBar.setProgress(firstBar.getProgress() + 100);
        }

        @Override
        protected void onPostExecute(ArrayList<SearchInfo> strings) {
            super.onPostExecute(strings);
            firstBar.setVisibility(View.INVISIBLE);
            firstBar.getLayoutParams().height = 0;

            messages = (ListView) findViewById(R.id.messageShow);

            if (strings != null) {
                for (SearchInfo s : strings) {
                    searchAdapter.insert(s,0);
                }
                searchAdapterSave = strings;
               // messages.setSelection(strings.size() + index);
            }

            int itemNum = searchAdapter.getCount();
            String result;
            if (itemNum == 1)
                result = (itemNum + " RESULT FOUND");
            else
                result = (itemNum + " RESULTS FOUND");
            results.setText(result);
            searchComplete = true;

            Toast.makeText(getBaseContext(), "Your search is done!", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("key", searchAdapterSave);
        super.onSaveInstanceState(outState);
    }

    public void update() {
        if(task.getStatus() != AsyncTask.Status.RUNNING) {
            searchComplete = false;
            results.setText("");
            firstBar.setProgress(0);
            firstBar.getLayoutParams().height = 50;
            firstBar.setVisibility(View.VISIBLE);
            searchAdapter.clear();
            beforeId = "";
            task = new Fetch();
            task.execute();
        }
        else {
            task.cancel(true);
            task = new Fetch();
            update();
        }
    }

}
