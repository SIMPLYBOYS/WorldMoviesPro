package com.github.florent37.materialviewpager.sample.fragment;

import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.github.florent37.materialviewpager.sample.R;
import com.squareup.picasso.Picasso;

/**
 * Created by aaron on 2016/7/28.
 */
public class InfoTabFragment extends Fragment {

    public void countryFlag (String location, ImageView thumbnailView) {

        switch (location) {
            case "France":
            case "Français":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.fr).into(thumbnailView);
                break;
            case "Germany":
            case "West Germany":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.gm).into(thumbnailView);
                break;
            case "일본":
            case "日本":
            case "Japan":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.japan).into(thumbnailView);
                break;
            case "Brazil":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.brazil).into(thumbnailView);
                break;
            case "Espagne":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Italy":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.it).into(thumbnailView);
                break;
            case "New Zealand":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.newzealand).into(thumbnailView);
                break;
            case "한국":
            case "South Korea":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.korea).into(thumbnailView);
                break;
            case "UK":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.uk).into(thumbnailView);
                break;
            case "Iran":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.iran).into(thumbnailView);
                break;
            case "India":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.india).into(thumbnailView);
                break;
            case "Lebanon":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.lebanon).into(thumbnailView);
                break;
            case "Spain":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Sweden":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.sweden).into(thumbnailView);
                break;
            case "Argentina":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.argentina).into(thumbnailView);
                break;
            case "Canada":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.canada).into(thumbnailView);
                break;
            case "Australia":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.australia).into(thumbnailView);
                break;
            case "Ireland":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ireland).into(thumbnailView);
                break;
            case "Mexico":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.mexico).into(thumbnailView);
                break;
            case "Soviet Union":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ru).into(thumbnailView);
                break;
            case "Hong Kong":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.hong_kong).into(thumbnailView);
                break;
            case "China":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.china).into(thumbnailView);
                break;
            case "Denmark":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.denmark).into(thumbnailView);
                break;
            case "Taiwan":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.taiwan).into(thumbnailView);
                break;
            case "Américain":
            case "미국":
            case "アメリカ":
            case "USA":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.usa).into(thumbnailView);
                break;
        }
    }
}
