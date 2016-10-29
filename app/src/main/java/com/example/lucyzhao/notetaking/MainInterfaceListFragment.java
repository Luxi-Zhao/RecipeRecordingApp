package com.example.lucyzhao.notetaking;

import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainInterfaceListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MainInterfaceListFragment extends ListFragment {


    private ArrayList<Food> foodList;
    public MainInterfaceListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        foodList = new ArrayList<>();
        ArrayAdapter<Food> adapter;
        adapter = new ArrayAdapter<>(getActivity(),
                            R.layout.single_list_item,foodList);

        setListAdapter(adapter);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_interface_list, container, false);
    }





}
