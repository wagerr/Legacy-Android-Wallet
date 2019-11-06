package com.wagerr.legacywallet.ui.settings_rates;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.wagerr.legacywallet.R;
import global.WagerrRate;
import com.wagerr.legacywallet.ui.base.BaseRecyclerFragment;
import com.wagerr.legacywallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import com.wagerr.legacywallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import com.wagerr.legacywallet.ui.base.tools.adapter.ListItemListeners;

/**
 * Created by furszy on 7/2/17.
 */

public class RatesFragment extends BaseRecyclerFragment<WagerrRate> implements ListItemListeners<WagerrRate> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmptyText("No rate available");
        setEmptyTextColor(Color.parseColor("#cccccc"));
        return view;
    }

    @Override
    protected List<WagerrRate> onLoading() {
        return wagerrModule.listRates();
    }

    @Override
    protected BaseRecyclerAdapter<WagerrRate, ? extends WagerrRateHolder> initAdapter() {
        BaseRecyclerAdapter<WagerrRate, WagerrRateHolder> adapter = new BaseRecyclerAdapter<WagerrRate, WagerrRateHolder>(getActivity()) {
            @Override
            protected WagerrRateHolder createHolder(View itemView, int type) {
                return new WagerrRateHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.rate_row;
            }

            @Override
            protected void bindHolder(WagerrRateHolder holder, WagerrRate data, int position) {
                holder.txt_name.setText(data.getCode());
                if (list.get(0).getCode().equals(data.getCode()))
                    holder.view_line.setVisibility(View.GONE);
            }
        };
        adapter.setListEventListener(this);
        return adapter;
    }

    @Override
    public void onItemClickListener(WagerrRate data, int position) {
        wagerrApplication.getAppConf().setSelectedRateCoin(data.getCode());
        Toast.makeText(getActivity(),R.string.rate_selected,Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }

    @Override
    public void onLongItemClickListener(WagerrRate data, int position) {

    }

    private  class WagerrRateHolder extends BaseRecyclerViewHolder{

        private TextView txt_name;
        private View view_line;

        protected WagerrRateHolder(View itemView, int holderType) {
            super(itemView, holderType);
            txt_name = (TextView) itemView.findViewById(R.id.txt_name);
            view_line = itemView.findViewById(R.id.view_line);
        }
    }
}
