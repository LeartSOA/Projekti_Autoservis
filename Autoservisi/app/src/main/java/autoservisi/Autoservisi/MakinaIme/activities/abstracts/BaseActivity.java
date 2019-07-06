package autoservisi.Autoservisi.MakinaIme.activities.abstracts;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import autoservisi.Autoservisi.MakinaIme.activities.interfaces.IBaseActivity;

public abstract class BaseActivity extends AppCompatActivity
                    implements IBaseActivity{

    protected void setToolbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    protected CharSequence getToolbarTitle() {
        ActionBar actionBar = getSupportActionBar();
        return actionBar == null ? "" : actionBar.getTitle();
    }

    protected void setBackNavigation() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
