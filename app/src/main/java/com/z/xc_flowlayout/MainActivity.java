package com.z.xc_flowlayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        List<String> list = new ArrayList<>();
        list.add("小米");
        list.add("360");
        list.add("samsung");
        list.add("huawei");
        list.add("oppo");
        list.add("vivo");

        FlowLayout layout = findViewById(R.id.flow_adapter);
        layout.setAdapter(new FlowAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public View getView(int position, ViewGroup parent) {
                TextView textView = (TextView) LayoutInflater.from(MainActivity.this).inflate(R.layout.item_tag, parent, false);
                textView.setText(list.get(position));
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "click: " + ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
                    }
                });
                return textView;
            }
        });
    }
}