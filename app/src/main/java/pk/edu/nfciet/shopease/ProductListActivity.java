package pk.edu.nfciet.shopease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            goToLogin();
            return;
        }

        TextView tvHello = findViewById(R.id.tvHello);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.getString("name");

                    if (name != null) {
                        tvHello.setText("Hi, " + name);
                    }
                });

        findViewById(R.id.btnLogout)
                .setOnClickListener(v -> {

                    FirebaseAuth.getInstance().signOut();
                    goToLogin();

                });
    }

    private void goToLogin() {

        Intent i = new Intent(
                this,
                LoginActivity.class
        );

        i.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(i);
    }
}