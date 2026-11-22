
package pk.edu.nfciet.shopease;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseUser user =
                    FirebaseAuth.getInstance().getCurrentUser();

            Class<?> next = (user != null)
                    ? ProductListActivity.class
                    : LoginActivity.class;

            startActivity(new Intent(this, next));
            finish();

        }, 1500);
    }
}
