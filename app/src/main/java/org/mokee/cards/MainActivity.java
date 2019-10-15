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
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CardsDbHelper mDbHelper;

    private List<Card> mCards;
    private CardsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new CardsDbHelper(this);

        mAdapter = new CardsAdapter(this);

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
        Toast.makeText(this, getString(R.string.card_active, card.name), Toast.LENGTH_SHORT).show();
    }

    private void handleEdit(CardsAdapter.ViewHolder holder, Card card) {
        final Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("card", card);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(holder.cardView, "card"),
                Pair.create(holder.nameView, "name")).toBundle());
    }

    private class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {

        private final LayoutInflater mInflater;

        CardsAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Card card = mCards.get(position);
            holder.cardView.setCardBackgroundColor(card.color);
            holder.nameView.setText(card.name);
            holder.cardView.setOnClickListener(v -> handleSelect(card));
            holder.cardView.setOnLongClickListener(v -> {
                handleEdit(holder, card);
                return true;
            });
        }

        @Override
        public long getItemId(int position) {
            return mCards.get(position).id;
        }

        @Override
        public int getItemCount() {
            return mCards != null ? mCards.size() : 0;
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
