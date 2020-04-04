package io.github.defolters.homeapp.widgets;

import android.content.Context;
import android.util.AttributeSet;

import io.github.defolters.homeapp.R;
import io.github.defolters.homeapp.data.ChartsLoader;
import io.github.defolters.homeapp.domain.Chart;

public class FollowersWidget extends BaseChartWidget {

    private ChartsLoader.Type type = ChartsLoader.Type.TEMPERATURE;

    public FollowersWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    void onRequestDetails(long date) {
        final long[] dates = new long[]{date};
        final int detailsDays = 7;
        ChartsLoader.loadDetails(getContext(), type, dates, detailsDays, this::onDetailsLoaded);
    }

    public void loadChart(Integer typeInt) {
        if (typeInt == 1) {
            main.titleText.setText(R.string.temperature);
            type = ChartsLoader.Type.TEMPERATURE;
        } else if (typeInt == 2) {
            main.titleText.setText(R.string.humidity);
            type = ChartsLoader.Type.HUMIDITY;
        } else if (typeInt == 3) {
            main.titleText.setText(R.string.co2);
            type = ChartsLoader.Type.CO2;
        }

        ChartsLoader.loadChart(getContext(), type, this::setMainChart);
    }

    private void onDetailsLoaded(Chart[] charts) {
        setDetailsChart(charts[0]);
    }

}
