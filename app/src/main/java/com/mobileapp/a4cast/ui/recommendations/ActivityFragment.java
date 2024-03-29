/*
 * Weather4cast
 * Robert Russell | Dongjun Gu
 * April/2023
 */
package com.mobileapp.a4cast.ui.recommendations;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mobileapp.a4cast.DatabaseItem;
import com.mobileapp.a4cast.GlobalData;
import com.mobileapp.a4cast.ModelClass;
import com.mobileapp.a4cast.R;
import com.mobileapp.a4cast.RecyclerViewAdaptor;
import com.mobileapp.a4cast.SQLiteManager;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment { // ACTIVITY
    View view;
    RecyclerViewAdaptor adapter;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<ModelClass> displayList;
    List<DatabaseItem> activityList, conditions, temps, rainList;
    private SQLiteManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbManager = new SQLiteManager(getContext());
        view = inflater.inflate(R.layout.fragment_activity, container, false);
        Button backButton = view.findViewById(R.id.activityBackButton);

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.GONE);

        // Get the conditions list from another fragment
        conditions = GlobalData.getInstance().getConditions();
        temps = GlobalData.getInstance().getTemps();
        Log.d("DEBUG", "ACTIVITY FRAGMENT: condition.size: " + conditions.size());
        Log.d("DEBUG", "ACTIVITY FRAGMENT: temp.size: " + temps.size());

        /**
         * Example:
         * activityList.get(INT).getName()
         * activityList.get(INT).getMinTemp()
         * activityList.get(INT).getMaxTemp()
         * activityList.get(INT).getType()
         * etc...
         */
        activityList = new ArrayList<>();
        //CREATE NEW LIST
        switch (GlobalData.getInstance().getCurrentConditions()) {
            case "RAIN":
            case "DRIZZLE":
            case "THUNDERSTORM":
            case "MIST":
                rainList = dbManager.getItemsByConditions("RAIN", false);
                for (int i = 0; i < rainList.size(); i++) {
                    //Log.d("DEBUG", "CLOTHES FRAGMENT: i: " + i);
                    if (rainList.get(i).getType().equals("ACTIVITY")) {
                        //Log.d("DEBUG", "CLOTHES FRAGMENT: forLoop: " + );
                        activityList.add(rainList.get(i));
                    }
                }
                displayList = new ArrayList<>();
                initData(activityList);
                initRecyclerView();
                break;
            default:
                for (int i = 0; i < conditions.size(); i++) {
                    DatabaseItem item1 = conditions.get(i);
                    // iterate over the second list
                    for (int j = 0; j < temps.size(); j++) {
                        DatabaseItem item2 = temps.get(j);
                        // check if the items are equal
                        if (item1.getName().equals(item2.getName())) {
                            // add the item to the commonItems list if it's present in both lists
                            if (item1.getType().equals("ACTIVITY")) {
                                Log.d("DEBUG", "ACTIVITY FRAGMENT: added: " + item1.getName());
                                activityList.add(item1);
                            }
                            break; // break out of the inner loop to avoid duplicates
                        }
                    }
                }
                displayList = new ArrayList<>();
                initData(activityList);
                initRecyclerView();
                break;
        }

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DEBUG", "ACTIVITY FRAGMENT: Back Button Pressed");
                NavDirections action = ActivityFragmentDirections.actionActivityFragmentToNavigationRecommendations();
                Navigation.findNavController(view).navigate(action);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d("DEBUG", "ACTIVITY FRAGMENT: ON_DESTROY");
        super.onDestroyView();

    }

    //Set initial data
    private void initData(List<DatabaseItem> mainList) { //CHANGE HERE
        for (int i = 0; i < mainList.size(); i++) {
            DatabaseItem dbItem = mainList.get(i);
            switch (dbItem.getName()) {
                case "CAFE":
                    displayList.add(new ModelClass(R.drawable.cafe, "Cafe", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SKI":
                    displayList.add(new ModelClass(R.drawable.ski, "Ski", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "ICE FISHING":
                    displayList.add(new ModelClass(R.drawable.icefishing, "Ice Fishing", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "ICE SKATE":
                    displayList.add(new ModelClass(R.drawable.iceskate, "Ice Skating", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "MUSEUM":
                    displayList.add(new ModelClass(R.drawable.museum, "Museum", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "PLAY":
                    displayList.add(new ModelClass(R.drawable.play, "Play", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "BOWLING":
                    displayList.add(new ModelClass(R.drawable.bowling, "Bowling", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "MOVIE":
                    displayList.add(new ModelClass(R.drawable.movie, "Movie", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "HIKE":
                    displayList.add(new ModelClass(R.drawable.hike, "Hike", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "BIKE":
                    displayList.add(new ModelClass(R.drawable.bike, "Bike", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "AMUSEMENT PARK":
                    displayList.add(new ModelClass(R.drawable.amusementpark, "Amusement Park", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "PICNIC":
                    displayList.add(new ModelClass(R.drawable.picnic, "Picnic", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "BEACH":
                    displayList.add(new ModelClass(R.drawable.beach, "Beach", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "CONCERT":
                    displayList.add(new ModelClass(R.drawable.concert, "Concert", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "WATER PARK":
                    displayList.add(new ModelClass(R.drawable.waterpark, "Water Park", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "WATER SPORTS":
                    displayList.add(new ModelClass(R.drawable.watersports, "Water Sports", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
            }
        }
    }

    private void initRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdaptor(displayList, 2);
        adapter.notifyDataSetChanged();
    }
}