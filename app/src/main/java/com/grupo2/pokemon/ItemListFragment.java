package com.grupo2.pokemon;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.grupo2.pokemon.databinding.FragmentItemListBinding;
import com.grupo2.pokemon.databinding.ItemListContentBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A fragment representing a list of Items. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link ItemDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListFragment extends Fragment {

    /**
     * Method to intercept global key events in the
     * item list fragment to trigger keyboard shortcuts
     * Currently provides a toast when Ctrl + Z and Ctrl + F
     * are triggered
     */
    ViewCompat.OnUnhandledKeyEventListenerCompat unhandledKeyEventListenerCompat = (v, event) -> {
        if (event.getKeyCode() == KeyEvent.KEYCODE_Z && event.isCtrlPressed()) {
            Toast.makeText(
                    v.getContext(),
                    "Undo (Ctrl + Z) shortcut triggered",
                    Toast.LENGTH_LONG
            ).show();
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_F && event.isCtrlPressed()) {
            Toast.makeText(
                    v.getContext(),
                    "Find (Ctrl + F) shortcut triggered",
                    Toast.LENGTH_LONG
            ).show();
            return true;
        }
        return false;
    };

    private FragmentItemListBinding binding;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentItemListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.addOnUnhandledKeyEventListener(view, unhandledKeyEventListenerCompat);

        RecyclerView recyclerView = binding.itemList;

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        View itemDetailFragmentContainer = view.findViewById(R.id.item_detail_nav_container);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().serializeNulls().create()
                ))
                .build();
        IPokemonApiService pokemonApiService = retrofit.create(IPokemonApiService.class);
        Call<PokemonFetchResults> call = pokemonApiService.getPokemons();

        call.enqueue(new Callback<PokemonFetchResults>() {
            @Override
            public void onResponse(Call<PokemonFetchResults> call,
                                   Response<PokemonFetchResults> response) {
                if (response.isSuccessful()) {
                    ArrayList<Pokemon> pokemonList = response.body().getResults();
                    View recyclerView = getActivity().findViewById(R.id.item_list);
                    assert recyclerView != null;
                    setupRecyclerView((RecyclerView) recyclerView,itemDetailFragmentContainer, pokemonList);
                } else {
                    Log.d("Error", "Something happened");
                    return;
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("Error", t.toString());
            }
        });
    }

    private void setupRecyclerView(
            RecyclerView recyclerView,
            View itemDetailFragmentContainer,
            ArrayList<Pokemon> pokemonList
    ) {

        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(
                pokemonList,itemDetailFragmentContainer
        ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Pokemon> mValues;
        private final View mItemDetailFragmentContainer;

        SimpleItemRecyclerViewAdapter(List<Pokemon> items,
                                      View itemDetailFragmentContainer) {
            mValues = items;
            mItemDetailFragmentContainer = itemDetailFragmentContainer;
        }

        private final View.OnClickListener mOnClickListener = new
                View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Integer index = (int) view.getTag();

                        Pokemon item = mValues.get(index);
                        Bundle arguments = new Bundle();
                        arguments.putInt(ItemDetailFragment.ARG_ITEM_ID.toString(), index+1);
                        arguments.putString(ItemDetailFragment.ARG_ITEM_NAME,
                                item.getName());
                        arguments.putString(ItemDetailFragment.ARG_DESCRIPTION,
                                item.getDescription());

                        Navigation.findNavController(view).navigate(R.id.show_item_detail, arguments);
                    }
                };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ItemListContentBinding binding =
                    ItemListContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.mIdView.setText(Integer.toString(position));
            holder.mContentView.setText(mValues.get(position).getName());

            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(mOnClickListener);

            /*holder.itemView.setOnClickListener(itemView -> {
                Pokemon item =
                        (Pokemon) itemView.getTag();
                Bundle arguments = new Bundle();
                arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                if (mItemDetailFragmentContainer != null) {
                    Navigation.findNavController(mItemDetailFragmentContainer)
                            .navigate(R.id.fragment_item_detail, arguments);
                } else {
                    Navigation.findNavController(itemView).navigate(R.id.show_item_detail, arguments);
                }
            });*/

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                /*
//                 * Context click listener to handle Right click events
//                 * from mice and trackpad input to provide a more native
//                 * experience on larger screen devices
//                 */
//                holder.itemView.setOnContextClickListener(v -> {
//                    Pokemon item =
//                            (Pokemon) holder.itemView.getTag();
//                    Toast.makeText(
//                            holder.itemView.getContext(),
//                            "Context click of item " + item.id,
//                            Toast.LENGTH_LONG
//                    ).show();
//                    return true;
//                });
//            }
//            holder.itemView.setOnLongClickListener(v -> {
//                // Setting the item id as the clip data so that the drop target is able to
//                // identify the id of the content
//                ClipData.Item clipItem = new ClipData.Item(mValues.get(position).id);
//                ClipData dragData = new ClipData(
//                        ((Pokemon) v.getTag()).content,
//                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
//                        clipItem
//                );
//
//                if (Build.VERSION.SDK_INT >= 24) {
//                    v.startDragAndDrop(
//                            dragData,
//                            new View.DragShadowBuilder(v),
//                            null,
//                            0
//                    );
//                } else {
//                    v.startDrag(
//                            dragData,
//                            new View.DragShadowBuilder(v),
//                            null,
//                            0
//                    );
//                }
//                return true;
//            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(ItemListContentBinding binding) {
                super(binding.getRoot());
                mIdView = binding.idText;
                mContentView = binding.content;
            }

        }

    }
}