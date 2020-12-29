package com.example.arudino_bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.arudino_bluetooth.R;

public class SettingActivity extends AppCompatActivity {

    Button btnCheck;
    EditText edGoal;

    private SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        btnCheck = findViewById(R.id.btn_check);
        edGoal = findViewById(R.id.ed_goal);

        int smoke_count = Integer.parseInt(pref.getString("smoke_count", "0"));

        btnCheck.setOnClickListener((view) -> {
            if (edGoal.getText().toString().isEmpty()) {
                Toast.makeText(this, "목표치를 입력하세요.", Toast.LENGTH_SHORT).show();
            } else if (smoke_count > Integer.parseInt(edGoal.getText().toString())) {
                Toast.makeText(this, "목표치가 흡연치를 초과했습니다. 다시 입력하세요.", Toast.LENGTH_SHORT).show();
            } else if (!edGoal.getText().toString().isEmpty()) {
                editor.putString("goal_count", edGoal.getText().toString());
                Toast.makeText(this, "목표치가 " + edGoal.getText().toString() + "개로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                editor.apply();
                finish();
            }
        });
    }
}
