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

public class ClothesFragment extends Fragment { // CLOTHES

    View view;
    RecyclerViewAdaptor adapter;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<ModelClass> displayList;
    List<DatabaseItem> clothesList, conditions, temps, rainList;
    private SQLiteManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbManager = new SQLiteManager(getContext());
        view = inflater.inflate(R.layout.fragment_clothes, container, false);
        Button backButton = view.findViewById(R.id.clothesBackButton);

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.GONE);

        // get the conditions list from another fragment
        conditions = GlobalData.getInstance().getConditions();
        temps = GlobalData.getInstance().getTemps();
        Log.d("DEBUG", "CLOTHES FRAGMENT: condition.size: " + conditions.size());
        Log.d("DEBUG", "CLOTHES FRAGMENT: temp.size: " + temps.size());

        /**
         * Example:
         * clothesList.get(INT).getName()
         * clothesList.get(INT).getMinTemp()
         * clothesList.get(INT).getMaxTemp()
         * clothesList.get(INT).getType()
         * clothesList.get(INT).getLink()
         * etc...
         */
        clothesList = new ArrayList<>();
        //CREATE NEW LIST
        for (int i = 0; i < conditions.size(); i++) {
            DatabaseItem item1 = conditions.get(i);
            // iterate over the second list
            for (int j = 0; j < temps.size(); j++) {
                DatabaseItem item2 = temps.get(j);
                // check if the items are equal
                if (item1.getName().equals(item2.getName())) {
                    // add the item to the commonItems list if it's present in both lists
                    if (item1.getType().equals("CLOTHING")) {
                        clothesList.add(item1);
                    }
                    break; // break out of the inner loop to avoid duplicates
                }
            }
        }
        //clothesList / Getting Rain info
        switch (GlobalData.getInstance().getCurrentConditions()) {
            case "RAIN":
            case "DRIZZLE":
            case "THUNDERSTORM":
            case "MIST":
                rainList = dbManager.getItemsByConditions("RAIN", false);
                for (int i = 0; i < rainList.size(); i++) {
                    //Log.d("DEBUG", "CLOTHES FRAGMENT: i: " + i);
                    if (rainList.get(i).getType().equals("CLOTHING")) {
                        //Log.d("DEBUG", "CLOTHES FRAGMENT: forLoop: " + );
                        clothesList.add(rainList.get(i));
                    }
                }
                break;
        }
        displayList = new ArrayList<>();
        initData();
        initRecyclerView();

        //Setup Recycler View
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DEBUG", "CLOTHES FRAGMENT: Back Button Pressed");
                NavDirections action = ClothesFragmentDirections.actionClothesFragmentToNavigationRecommendations();
                Navigation.findNavController(view).navigate(action);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d("DEBUG", "CLOTHES FRAGMENT: ON_DESTROY");
        super.onDestroyView();

    }

    //Set initial data
    private void initData() {
        for (int i = 0; i < clothesList.size(); i++) {
            DatabaseItem dbItem = clothesList.get(i);
            switch (dbItem.getName()) {
                case "LEGGINGS":
                    displayList.add(new ModelClass(R.drawable.leggings, "Leggings", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "EARMUFFS":
                    displayList.add(new ModelClass(R.drawable.earmuffs, "Earmuffs", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "GLOVES":
                    displayList.add(new ModelClass(R.drawable.gloves, "Gloves", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "WOOLEN HAT":
                    displayList.add(new ModelClass(R.drawable.woolenhat, "Woolen Hat", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SWEATER":
                    displayList.add(new ModelClass(R.drawable.sweater, "Sweater", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "PADDED JACKET":
                    displayList.add(new ModelClass(R.drawable.paddedjacket, "Padded Jacket", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "FLEECE-LINED PANTS":
                    displayList.add(new ModelClass(R.drawable.fleecelinedpants, "Fleece-lined Pants", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "LONG UNDERWEAR":
                    displayList.add(new ModelClass(R.drawable.longunderwear, "Long Underwear", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "COAT":
                    displayList.add(new ModelClass(R.drawable.coat, "Coat", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "BOOTS":
                    displayList.add(new ModelClass(R.drawable.boots, "Boots", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "JEANS":
                    displayList.add(new ModelClass(R.drawable.jeans, "Jeans", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "TRENCH COAT":
                    displayList.add(new ModelClass(R.drawable.trenchcoat, "Trench Coat", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "HOOD T-SHIRT":
                    displayList.add(new ModelClass(R.drawable.hoodtshirt, "Hood T-Shirt", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SNEAKERS":
                    displayList.add(new ModelClass(R.drawable.sneakers, "Sneakers", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "LOAFERS":
                    displayList.add(new ModelClass(R.drawable.loafers, "Loafer", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "LEATHER JACKET":
                    displayList.add(new ModelClass(R.drawable.leatherjacket, "Leather Jacket", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "CHINO PANTS":
                    displayList.add(new ModelClass(R.drawable.chinopants, "Chino Pants", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "T-SHIRTS":
                    displayList.add(new ModelClass(R.drawable.tshirts, "T-Shirts", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "JACKET":
                    displayList.add(new ModelClass(R.drawable.jacket, "Jacket", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "CARDIGAN":
                    displayList.add(new ModelClass(R.drawable.cardigan, "Cardigan", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "DRESS SHIRTS":
                    displayList.add(new ModelClass(R.drawable.dressshirts, "Dress Shirts", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SUNGLASSES":
                    displayList.add(new ModelClass(R.drawable.sunglasses, "Sunglasses", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SLEEVELESS":
                    displayList.add(new ModelClass(R.drawable.sleeveless, "Sleeveless", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SHORTS":
                    displayList.add(new ModelClass(R.drawable.shorts, "Shorts", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "ONE PIECE":
                    displayList.add(new ModelClass(R.drawable.onepiece, "One Piece", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "SANDAL":
                    displayList.add(new ModelClass(R.drawable.sandal, "Sandals", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "UMBRELLA":
                    displayList.add(new ModelClass(R.drawable.umbrella, "Umbrella", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "RAINCOAT":
                    displayList.add(new ModelClass(R.drawable.raincoat, "Raincoat", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
                case "RAINBOOTS":
                    displayList.add(new ModelClass(R.drawable.rainboots, "Rainboots", dbItem.getLink(), dbItem.getRecipe(), dbItem.getComment()));
                    break;
            }
        }
    }

    private void initRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdaptor(displayList, 1);
        adapter.notifyDataSetChanged();
    }
}

