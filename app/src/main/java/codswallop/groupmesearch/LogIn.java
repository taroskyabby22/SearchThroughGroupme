package codswallop.groupmesearch;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class LogIn extends AppCompatActivity {

    String accessToken = "";
    SharedPreferences sharedpreferences;
    final String url = "https://oauth.groupme.com/oauth/login_dialog?client_id=LeVreYTR2wRecRdpzuE71EarJvwQZGsfYPsqs8YpHYueRvxn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

            String MyPREFERENCES = "MyPrefs";
            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            accessToken = sharedpreferences.getString("ACCESS", "NA");
            if (!accessToken.equals("NA")) {
                Intent intent = new Intent(LogIn.this, PickGroup.class);
                intent.putExtra("ACCESS", accessToken);
                startActivity(intent);
            }


    }

    public void goTo(View v)
    {
        Intent inent = new Intent(Intent.ACTION_VIEW);
        inent.setData(Uri.parse(url));
        startActivity(inent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = this.getIntent().getData();
        try {
            String url = uri.toString();
            String fragment = "?access_token=";
            int start = url.indexOf(fragment);
            if (start > -1) {
                // You can use the accessToken for api calls now.
                accessToken = url.substring(start + fragment.length(), url.length());
                final Dialog dialog = new Dialog(LogIn.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.save_popup);
                dialog.setCancelable(false);
                dialog.show();

                Button yespref = (Button) dialog.findViewById(R.id.saveyes);
                yespref.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String MyPREFERENCES = "MyPrefs";
                        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("ACCESS", accessToken);
                        editor.commit();

                        Intent intent = new Intent(LogIn.this, PickGroup.class);
                        intent.putExtra("ACCESS", accessToken);
                        startActivity(intent);
                    }
                });

                Button nopref = (Button) dialog.findViewById(R.id.saveno);
                nopref.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(LogIn.this, PickGroup.class);
                        intent.putExtra("ACCESS", accessToken);
                        startActivity(intent);
                    }
                });

            }

        }
        catch (Exception e){}
    }
}