package codswallop.groupmesearch;

import android.app.Activity;
import android.text.format.DateUtils;
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


public class GroupListAdapter extends ArrayAdapter<PathId> {

    public GroupListAdapter(Activity context, ArrayList<PathId> androidFlavors) {
        super(context, 0, androidFlavors);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        PathId info = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }


        final ImageView iconView = (ImageView) convertView.findViewById(R.id.list_icon);
        Picasso.with(getContext()).load(info.image).into(iconView);

        final TextView textView = (TextView) convertView.findViewById(R.id.list_text);
        textView.setText(info.name);

        final TextView preview = (TextView) convertView.findViewById(R.id.list_text_description);
        preview.setText(info.preview);

        final TextView date = (TextView) convertView.findViewById(R.id.list_date);
        date.setText(getDate(info.time));

        return convertView;
    }

    public PathId getId(int position) {
        PathId info = getItem(position);
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
            else if(isDateInCurrentYear(message_date))
                sdf = new SimpleDateFormat("MMM dd");

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

    public static boolean isDateInCurrentYear(long dateL) {
        Date date = new Date(dateL);
        Calendar currentCalendar = Calendar.getInstance();
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return year == targetYear;
    }

}

