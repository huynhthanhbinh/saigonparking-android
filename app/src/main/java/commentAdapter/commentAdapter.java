package commentAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.vtb.parkingmap.R;
import com.vtb.parkingmap.models.Cardcomment;

import java.util.ArrayList;

public class commentAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<Cardcomment> modelArrayList;

    public commentAdapter(Context context, ArrayList<Cardcomment> modelArrayList) {
        this.context = context;
        this.modelArrayList = modelArrayList;
    }

    @Override
    public int getCount() {
        return modelArrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = LayoutInflater.from(context).inflate(R.layout.card_item_comment, container, false);

        TextView UserNameCardItem = view.findViewById(R.id.UserNameCardItem);
        TextView CommentCardItem = view.findViewById(R.id.CommentCardItem);
        TextView DateCardItem = view.findViewById(R.id.DateCardItem);
        RatingBar RatingCardItem = view.findViewById(R.id.ratingStart);

        Cardcomment model = modelArrayList.get(position);
        String Username = model.getUsername();
        String Comment = model.getComment();
        String Date = model.getDate();
        int Rating = model.getRating();

        UserNameCardItem.setText(Username);
        CommentCardItem.setText(Comment);
        DateCardItem.setText(Date);
        RatingCardItem.setNumStars(Rating);

        container.addView(view, position);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
