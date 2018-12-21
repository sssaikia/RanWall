package com.sstudio.ranwall;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Alan on 5/16/2018.
 */

public class BottomClass extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet, container, false);
        ((TextView) v.findViewById(R.id.feedback)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://www.easypolls.net/poll.html?p=5af97543e4b064438a32440f"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        ((SwitchCompat) v.findViewById(R.id.toggle)).setChecked(true);
        ((SwitchCompat) v.findViewById(R.id.toggle)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getContext(), "working", Toast.LENGTH_SHORT).show();
                if (b) {
                    Intent startIntent = new Intent(getContext(), RanWallService.class);
                    startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    getContext().startService(startIntent);
                } else {
                    Toast.makeText(getContext(), "May not work properly", Toast.LENGTH_SHORT).show();
                    Intent stopIntent = new Intent(getContext(), RanWallService.class);
                    stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                    getContext().startService(stopIntent);
                }
            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
