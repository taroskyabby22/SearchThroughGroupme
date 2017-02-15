package codswallop.groupmesearch;

import android.app.Activity;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Abby on 7/11/2016.
 */
public class SearchAdapter extends ArrayAdapter<SearchInfo> {
    private static final String LOG_TAG = GroupListAdapter.class.getSimpleName();

    public SearchAdapter(Activity context, ArrayList<SearchInfo> androidFlavors) {
        super(context, 0, androidFlavors);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        SearchInfo info = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_list_item, parent, false);
        }

        final ImageView iconView = (ImageView) convertView.findViewById(R.id.list_icon);
        try {
            Picasso.with(getContext()).load(info.avatar_url).into(iconView);
        } catch (Exception e) {
        }

        final TextView textView = (TextView) convertView.findViewById(R.id.list_text);
        textView.setText(info.name);

        final TextView preview = (TextView) convertView.findViewById(R.id.list_text_description);
        preview.setText(info.text);

        final TextView date = (TextView) convertView.findViewById(R.id.list_date);
        date.setText(getDate(info.created_at));

        return convertView;
    }

    public SearchInfo getId(int position) {
        SearchInfo info = getItem(position);
        return info;
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

    public static boolean isDateInCurrentWeek(long dateL) {
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