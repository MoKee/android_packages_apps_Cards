/*
 * Copyright (C) 2019 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mokee.cards;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drakeet.multitype.ItemViewBinder;
import com.drakeet.multitype.MultiTypeAdapter;
import com.google.android.material.appbar.AppBarLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SharedPreferences mPref;
    private CardsDbHelper mDbHelper;

    private List<Card> mCards;
    private MultiTypeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mDbHelper = new CardsDbHelper(this);

        mAdapter = new MultiTypeAdapter();
        mAdapter.setHasStableIds(true);
        mAdapter.register(SubHeaderItem.class, new SubHeaderItemViewBinder());
        mAdapter.register(CardItem.class, new CardItemViewBinder());

        final AppBarLayout appBar = findViewById(R.id.appbar);
        final float overlapDistance = getResources().getDimension(R.dimen.appbar_overlap_distance);
        final float overlapElevation = getResources().getDimension(R.dimen.appbar_overlap_elevation);

        final RecyclerView peersView = findViewById(R.id.peers);
        peersView.setAdapter(mAdapter);
        peersView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                final int offset = peersView.computeVerticalScrollOffset();
                final float progress = Math.min((float) offset / overlapDistance, 1.0f);
                appBar.setElevation(overlapElevation * progress);
            }
        });

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCards = mDbHelper.query();
        rebuildList();
    }

    private void rebuildList() {
        final List<Object> list = new ArrayList<>();
        final int selectedId = mPref.getInt("selected", -1);

        Card selected = null;
        final List<Card> cards = new ArrayList<>();

        for (Card card : mCards) {
            if (card.id == selectedId) {
                selected = card;
            } else {
                cards.add(card);
            }
        }

        if (selected != null) {
            final SubHeaderItem subHeaderItem = new SubHeaderItem();
            subHeaderItem.title = R.string.sub_active;
            list.add(subHeaderItem);

            final CardItem cardItem = new CardItem();
            cardItem.card = selected;
            list.add(cardItem);
        }

        if (!cards.isEmpty()) {
            final SubHeaderItem subHeaderItem = new SubHeaderItem();
            subHeaderItem.title = R.string.sub_available;
            list.add(subHeaderItem);

            for (Card card : cards) {
                final CardItem cardItem = new CardItem();
                cardItem.card = card;
                list.add(cardItem);
            }
        }

        mAdapter.setItems(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enroll:
                startActivity(new Intent(this, EnrollActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleSelect(Card card) {
        final Intent intent = new Intent();
        intent.setClassName("com.android.nfc", "org.mokee.nfc.MoKeeNfcSetIdReceiver");
        intent.putExtra("uid", card.uid);
        sendBroadcast(intent);

        if (mPref.edit().putInt("selected", card.id).commit()) {
            rebuildList();
        }

        Toast.makeText(this, getString(R.string.card_active, card.name), Toast.LENGTH_SHORT).show();
    }

    private void handleEdit(CardItemViewBinder.ViewHolder holder, Card card) {
        final Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("card", card);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(holder.cardView, "card"),
                Pair.create(holder.nameView, "name")).toBundle());
    }

    private class SubHeaderItem {
        @StringRes
        private int title;
    }

    private class CardItem {
        private Card card;
    }

    private class SubHeaderItemViewBinder extends ItemViewBinder<SubHeaderItem, SubHeaderItemViewBinder.ViewHolder> {

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull LayoutInflater inflater,
                                             @NotNull ViewGroup parent) {
            return new ViewHolder(inflater.inflate(R.layout.item_subheader, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, SubHeaderItem item) {
            holder.titleView.setText(item.title);
        }

        @Override
        public long getItemId(SubHeaderItem item) {
            return 0x0100000000000000L | item.title;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView titleView;

            private ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.title);
            }

        }

    }

    private class CardItemViewBinder extends ItemViewBinder<CardItem, CardItemViewBinder.ViewHolder> {

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull LayoutInflater inflater,
                                             @NotNull ViewGroup parent) {
            return new ViewHolder(inflater.inflate(R.layout.item_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, CardItem item) {
            holder.cardView.setCardBackgroundColor(item.card.color);
            holder.nameView.setText(item.card.name);
            holder.cardView.setOnClickListener(v -> handleSelect(item.card));
            holder.cardView.setOnLongClickListener(v -> {
                handleEdit(holder, item.card);
                return true;
            });
        }

        @Override
        public long getItemId(CardItem item) {
            return 0x0200000000000000L | item.card.id;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            CardView cardView;
            TextView nameView;

            private ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card);
                nameView = itemView.findViewById(R.id.name);
            }

        }

    }

}
