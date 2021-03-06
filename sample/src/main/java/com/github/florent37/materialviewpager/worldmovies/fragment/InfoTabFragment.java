package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.github.florent37.materialviewpager.worldmovies.R;
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
            case "Deutschland":
            case "West Germany":
            case "德國":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.gm).into(thumbnailView);
                break;
            case "일본":
            case "日本":
            case "Japan":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.japan).into(thumbnailView);
                break;
            case "中國香港":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.hong_kong).into(thumbnailView);
                break;
            case "Brazil":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.brazil).into(thumbnailView);
                break;
            case "Italy":
            case "義大利":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.it).into(thumbnailView);
                break;
            case "New Zealand":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.newzealand).into(thumbnailView);
                break;
            case "韓國":
            case "한국":
            case "Korea":
            case "South Korea":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.korea).into(thumbnailView);
                break;
            case "UK":
            case "Britannique":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.uk).into(thumbnailView);
                break;
            case "Iran":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.iran).into(thumbnailView);
                break;
            case "India":
            case "印度":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.india).into(thumbnailView);
                break;
            case "Lebanon":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.lebanon).into(thumbnailView);
                break;
            case "Spain":
            case "西班牙":
            case "Espagne":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Türkei":
            case "Turkey":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.turkey).into(thumbnailView);
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
            case "澳大利亞":
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
            case "香港":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.hong_kong).into(thumbnailView);
                break;
            case "China":
            case "中國大陸":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.china).into(thumbnailView);
                break;
            case "Denmark":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.denmark).into(thumbnailView);
                break;
            case "Taiwan":
            case "臺灣":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.taiwan).into(thumbnailView);
                break;
            case "Thailand":
            case "泰國":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.thailand).into(thumbnailView);
                break;
            case "Russia":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ru).into(thumbnailView);
                break;
            case "Américain":
            case "미국":
            case "アメリカ":
            case "USA":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.usa).into(thumbnailView);
                break;
            case "Poland":
            case "波蘭":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.poland).into(thumbnailView);
                break;
        }
    }
}
