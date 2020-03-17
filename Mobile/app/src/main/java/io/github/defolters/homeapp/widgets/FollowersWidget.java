package io.github.defolters.homeapp.widgets;

import android.content.Context;
import android.util.AttributeSet;


import io.github.defolters.homeapp.R;
import io.github.defolters.homeapp.data.ChartsLoader;
import io.github.defolters.homeapp.domain.Chart;

public class FollowersWidget extends BaseChartWidget {

    private static final ChartsLoader.Type TYPE = ChartsLoader.Type.FOLLOWERS;

    public FollowersWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        main.titleText.setText(R.string.chart_title_followers);

        ChartsLoader.loadChart(context, TYPE, this::setMainChart);
    }

    @Override
    void onRequestDetails(long date) {
        final long[] dates = new long[] { date };
        final int detailsDays = 7;
        ChartsLoader.loadDetails(getContext(), TYPE, dates, detailsDays, this::onDetailsLoaded);
    }

    private void onDetailsLoaded(Chart[] charts) {
        setDetailsChart(charts[0]);
    }

}
