package org.taitascioredev.fractal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.taitascioredev.adapters.ActionAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 12/08/16.
 */
public class ActionsBottomSheet extends BottomSheetDialogFragment {

    @BindView(R.id.list) RecyclerView mRecyclerView;
    ActionAdapter mAdapter;
    LinearLayoutManager mLayoutMngr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_actions, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLayoutMngr = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutMngr);
        mAdapter = new ActionAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }
}
