package codswallop.groupmesearch;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Choice extends AppCompatActivity {
    String START = "https://api.groupme.com/v3/groups/";
    String LAST = "/messages?token=";
    String LAST2 = "&limit=100";
    String accessId;
    PathId GROUP;

    int countTo=0;
    int getCountTo = 0;

    boolean high = false;
    View highlight = null;

    String afterId = "";
    String beforeId = "";
    int ISSELECTED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        Intent intent = getIntent();
        beforeId = intent.getStringExtra("BEFOREID");
        GROUP = (PathId) getIntent().getSerializableExtra("GROUP");
        accessId = intent.getStringExtra("ACCESS");
        this.setTitle(GROUP.name + ": Search");
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra("PathId", GROUP);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    public class Fetch extends AsyncTask<Void, Integer, ArrayList<SearchInfo>> {

        private ArrayList<SearchInfo> getUrls(String json, ArrayList<SearchInfo> all) throws JSONException {
            String currentItem = "";
            String itemNoAt = "";
            String avatar = "";
            String name = "";
            String text = "";
            String attach = "";
            String created_at = "";
            String preview_attach = "";

            json = json.substring(json.indexOf("messages\":[") + "messages\":[".length());
            int numthrough = (json.length() - json.replace("},{\"atta", "").length()) / "},{\"atta".length() + 1;
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
                    getCountTo++;
                } else if (itemNoAt.contains("\"video\"")) {
                    attach = find("\"preview_attach\":", itemNoAt, "\"type\":");
                    preview_attach = find("\"url\":", itemNoAt, "}");
                    getCountTo++;
                }


                currentItem = currentItem.replace(itemNoAt, "[]");
                String sender_id = find("\"sender_id\":", currentItem, "\"sender_type\":");
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
                String id = find("\"id\":", currentItem, "\"name\":");

                all.add(new SearchInfo(avatar, created_at, "", "", id, name, sender_id, "", "", "", text, "", attach, preview_attach, ""));
                preview_attach = "";
                attach = "";
            }

            if (!afterId.equals(""))
                afterId = "";
            if (!beforeId.equals("")) {
                Collections.reverse(all);
                afterId = all.get(all.size() - 1).id;
                beforeId = "";
                ISSELECTED = all.size();
            }

            return all;
        }

        public String find(String what, String json, String next) {
            json = json.substring(json.indexOf(what) + what.length());
            json = json.substring(0, json.indexOf(next) - 1);
            if(json.equals(""))
                return "";
            else {
                if (json.charAt(0) == '"') {
                    json = json.substring(1);
                }
                if (json.charAt(json.length() - 1) == '"')
                    json = json.substring(0, json.length() - 1);
                if (json.equals("null"))
                    json = "";
                json = json.replace("\\\"", "\"");
                json = json.replace("\\n", "\n");
            }
            return json;
        }


        /**
         * get info from api
         */

        int i = 0;

        @Override
        protected ArrayList<SearchInfo> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //for raw JSON responce
            String forcastJsonStr = null;
            ArrayList<SearchInfo> all = new ArrayList<>();

            int count = 0;
            long first = 0;
            boolean inside = false;

            while (true) {
                try {
                    Uri build;
                    if (!beforeId.equals("")) {
                        build = Uri.parse(START + GROUP.id + LAST + accessId + LAST2 + "&before_id=" + beforeId);
                    } else if (!afterId.equals("")) {
                        build = Uri.parse(START + GROUP.id + LAST  + accessId + LAST2+ "&after_id=" + afterId);
                    } else
                        build = Uri.parse(START + GROUP.id + LAST  + accessId + LAST2+ "&after_id=" + "NO");
                    URL url = new URL(build.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    count++;

                    //read input
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null)
                        break;
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\n");
                    }

                    if (buffer.length() == 0) {
                        if (!beforeId.equals(""))
                            afterId = beforeId;
                        else
                            break;
                    }

                    forcastJsonStr = buffer.toString();
                } catch (IOException e) {
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

                try {
                    all = getUrls(forcastJsonStr, all);
                } catch (Exception e) {
                    Log.e("Error", "Exception", e);
                    if (!beforeId.equals("")) {
                        afterId = beforeId;
                        beforeId = "";
                    } else
                        break;
                }
            }

            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                }
            }

            return all;
        }

        @Override
        protected void onPostExecute(ArrayList<SearchInfo> strings) {
            super.onPostExecute(strings);


            //NEW ALSO
            final ScrollView scroll = (ScrollView) findViewById(R.id.scroll2);
            LinearLayout addTo = (LinearLayout) findViewById(R.id.puthere2);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            if (strings != null && strings.size() > 0) {
                strings.get(ISSELECTED).selected = "true";
                for (final SearchInfo s : strings) {
                    View itemView = inflater.inflate(R.layout.find_search_list, null);

                    if (s.selected.equals("true")) {
                        itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
                        highlight = itemView;
                        high = true;
                    }
                    final ImageView iconView = (ImageView) itemView.findViewById(R.id.list_icon2);
                    try {
                        Picasso.with(itemView.getContext()).load(s.avatar_url).into(iconView);
                    } catch (Exception e) {
                        iconView.setImageResource(R.drawable.notfound);
                    }

                    final TextView textView = (TextView) itemView.findViewById(R.id.list_text2);
                    textView.setText(s.name);

                    final TextView preview = (TextView) itemView.findViewById(R.id.list_text_description2);
                    preview.setText(s.text);


                    final ImageView attach = (ImageView) itemView.findViewById(R.id.attach2);
                    if (!s.attach.equals("")) {

                        Picasso.with(itemView.getContext())
                                .load(s.attach)
                                .into(attach, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        countTo++;
                                        new Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(getCountTo == countTo){
                                                    scroll.scrollTo(0, (int) highlight.getY());
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });


                        attach.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri;
                                if (!s.preview_attach.equals(""))
                                    uri = Uri.parse(s.preview_attach);
                                else
                                    uri = Uri.parse(s.attach);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                    } else {
                        attach.getLayoutParams().height = 0;
                        attach.setVisibility(View.INVISIBLE);
                    }


                    final TextView date = (TextView) itemView.findViewById(R.id.list_date2);
                    date.setText(getDate(s.created_at));
                    if (highlight != null && high) {
                        highlight = itemView;
                        addTo.addView(highlight);
                        high = false;
                        date.setTextColor(Color.parseColor("#FFFFFF"));
                    } else
                        addTo.addView(itemView);

                }

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if(getCountTo == 0){
                            scroll.scrollTo(0, (int) highlight.getY());
                        }
                    }
                });
            }
        }


        private String getDate(String time) {
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("MMM dd, yyyy");

            try {
                long message_date = Long.parseLong(time) * 1000;

                if(DateUtils.isToday(message_date))
                    sdf = new SimpleDateFormat("h:mm a");
                else if(isDateInCurrentWeek(message_date))
                    sdf = new SimpleDateFormat("EEE, h:mm a");

                return sdf.format(message_date);
            } catch (Exception ex) {
                return "xx";
            }
        }

        public boolean isDateInCurrentWeek(long dateL) {
            Date date = new Date(dateL);
            Calendar currentCalendar = Calendar.getInstance();
            int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
            int year = currentCalendar.get(Calendar.YEAR);
            Calendar targetCalendar = Calendar.getInstance();
            targetCalendar.setTime(date);
            int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
            int targetYear = targetCalendar.get(Calendar.YEAR);
            return week == targetWeek && year == targetYear;
        }

    }

    public void update() {
        Fetch task = new Fetch();
        task.execute();
    }

}
