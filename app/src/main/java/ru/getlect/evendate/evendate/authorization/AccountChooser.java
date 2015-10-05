package ru.getlect.evendate.evendate.authorization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;
import com.joanzapata.iconify.widget.IconTextView;

import ru.getlect.evendate.evendate.R;

/**
 * Created by fj on 14.09.2015.
 */
public class AccountChooser extends Activity implements View.OnClickListener {

    IconTextView itv_vk;
    IconTextView itv_fb;
    IconTextView itv_gPlus;

    LinearLayout ll_vk;
    LinearLayout ll_fb;
    LinearLayout ll_gplus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_chooser);

        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());


        itv_vk = (IconTextView)findViewById(R.id.itv_vk);
        itv_fb = (IconTextView)findViewById(R.id.itv_fb);
        itv_gPlus = (IconTextView)findViewById(R.id.itv_gPlus);

        itv_vk.setText("{entypo-vk}");
        itv_fb.setText("{entypo-facebook}");
        itv_gPlus.setText("{entypo-google}");

        ll_vk = (LinearLayout)findViewById(R.id.ll_vk);
        ll_fb = (LinearLayout)findViewById(R.id.ll_fb);
        ll_gplus = (LinearLayout)findViewById(R.id.ll_gplus);

        ll_vk.setOnClickListener(this);
        ll_fb.setOnClickListener(this);
        ll_gplus.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        int accountType=0;
        String accType = "accountType";
        Intent intent = new Intent(this,WebAuthActivity.class);

        switch (v.getId()){
            case R.id.ll_vk:
                accountType=0;
                intent.putExtra(accType,accountType);
                startActivity(intent);
                break;
            case R.id.ll_fb:
                accountType=1;
                intent.putExtra(accType,accountType);
                break;
            case R.id.ll_gplus:
                accountType=2;
                intent.putExtra(accType,accountType);
                break;
        }
        startActivity(intent);
    }
}
