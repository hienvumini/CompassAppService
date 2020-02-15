package com.nextsol.digitalcompass.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.NumderUltils;
import com.nextsol.digitalcompass.model.Forecast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForeCastAdapter extends RecyclerView.Adapter<ForeCastAdapter.MyViewHolder> {
    Context mctx;
    int layout;
    List<Forecast> forecasts;

    public ForeCastAdapter(Context context, int resource, List<Forecast> object) {
        this.mctx = context;
        this.layout = resource;
        this.forecasts = object;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mctx).inflate(R.layout.item_forecast, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        Locale locale=Locale.US;
        long timestamp = (forecasts.get(position).getTimeStamp());
        Date date = new Date(timestamp * 1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM d",locale);
       if (position!=0){
           holder.textViewDay.setText("" + simpleDateFormat.format(date));

       } else {
           holder.textViewDay.setText("Today");

       }
        holder.textViewStatus.setText("" + forecasts.get(position).getStatus());
        holder.textViewHumidity.setText("" + forecasts.get(position).getHumidity()+"%");
        holder.textViewWind.setText("" + forecasts.get(position).getSpeedWind()+"km/h, "+
                forecasts.get(position).getDirWind()+"° "+NumderUltils
                .getsymbolDirection(forecasts.get(position).getDirWind()));
        holder.textViewPressure.setText("" + forecasts.get(position).getPressure()+"mb");
        holder.textViewTemp.setText("" + (int) (forecasts.get(position).getTemp())+"°C");

        holder.imageViewIcon.setImageResource(forecasts.get(position).getIcon());
        holder.linearLayoutShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.isexpand = !holder.isexpand;

                if (holder.isexpand) {
                    holder.linearLayoutDetail.setVisibility(View.VISIBLE);

                } else {
                    holder.linearLayoutDetail.setVisibility(View.GONE);

                }
            }
        });


    }

    @Override
    public int getItemCount() {
        if (forecasts != null) {
            return forecasts.size();

        } else {

            return 0;
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIcon;
        TextView textViewDay, textViewStatus, textViewTemp, textViewWind, textViewHumidity, textViewPressure;
        LinearLayout linearLayoutDetail;
        LinearLayout linearLayoutShort;

        boolean isexpand;

        public MyViewHolder(View itemView) {

            super(itemView);
            imageViewIcon = (ImageView) itemView.findViewById(R.id.imageviewIcon_ItermForeCast);
            textViewTemp = (TextView) itemView.findViewById(R.id.textviewTemp_ItermForeCast);
            textViewDay = (TextView) itemView.findViewById(R.id.textviewDay_ItermForeCast);
            textViewWind = (TextView) itemView.findViewById(R.id.textviewWind_ItermForeCast);
            textViewHumidity = (TextView) itemView.findViewById(R.id.textviewHumidity_ItermForeCast);
            textViewPressure = (TextView) itemView.findViewById(R.id.textviewPressure_ItermForeCast);
            textViewStatus = (TextView) itemView.findViewById(R.id.textviewStatus_ItermForeCast);
            linearLayoutDetail = (LinearLayout) itemView.findViewById(R.id.linelerDetail_ItemrForeCast);
            linearLayoutShort = (LinearLayout) itemView.findViewById(R.id.lineershort_ItemForeCast);

        }
    }
}


