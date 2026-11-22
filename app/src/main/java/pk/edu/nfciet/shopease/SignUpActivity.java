
package pk.edu.nfciet.shopease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirm;
    private Button btnSignUp;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnSignUp = findViewById(R.id.btnSignUp);
        progress = findViewById(R.id.progress);

        btnSignUp.setOnClickListener(v -> signUp());

        findViewById(R.id.tvGoToLogin)
                .setOnClickListener(v -> finish());
    }

    private void signUp() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.err_name));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_email));
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.err_password));
            return;
        }

        if (!password.equals(confirm)) {
            etConfirm.setError(getString(R.string.err_match));
            return;
        }

        setLoading(true);

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result ->
                        saveProfile(
                                result.getUser().getUid(),
                                name,
                                email
                        )
                )
                .addOnFailureListener(this::showError);
    }

    private void saveProfile(
            String uid,
            String name,
            String email
    ) {

        Map<String, Object> user = new HashMap<>();

        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(v -> {

                    Intent i = new Intent(
                            this,
                            ProductListActivity.class
                    );

                    i.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(i);
                })
                .addOnFailureListener(this::showError);
    }

    private void showError(Exception e) {
        setLoading(false);

        Toast.makeText(
                this,
                e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnSignUp.setEnabled(!loading);
    }
}
