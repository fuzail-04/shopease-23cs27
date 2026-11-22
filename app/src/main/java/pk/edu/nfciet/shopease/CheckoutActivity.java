
package pk.edu.nfciet.shopease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etName, etPhone, etCity, etAddress;
    private Button btnPlace;
    private ProgressBar progress;

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private CollectionReference cartRef;
    private List<DocumentSnapshot> cartDocs = new ArrayList<>();

    private long subtotal, delivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        etAddress = findViewById(R.id.etAddress);
        btnPlace = findViewById(R.id.btnPlaceOrder);
        progress = findViewById(R.id.progress);

        btnPlace.setEnabled(false);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }

        cartRef = db.collection("carts")
                .document(uid)
                .collection("items");

        cartRef.get()
                .addOnSuccessListener(snap -> {
                    cartDocs = snap.getDocuments();
                    subtotal = 0;

                    for (DocumentSnapshot d : cartDocs) {
                        Long price = d.getLong("price");
                        Long quantity = d.getLong("quantity");

                        if (price != null && quantity != null) {
                            subtotal += price * quantity;
                        }
                    }

                    delivery = (subtotal == 0 || subtotal >= 5000)
                            ? 0 : 200;

                    ((TextView) findViewById(R.id.tvSummary))
                            .setText(cartDocs.size()
                                    + " items · Total: Rs "
                                    + (subtotal + delivery));

                    btnPlace.setEnabled(!cartDocs.isEmpty());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Could not load cart",
                                Toast.LENGTH_LONG).show());

        btnPlace.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Enter your name");
            return;
        }

        if (!phone.matches("03\\d{9}")) {
            etPhone.setError("11 digits starting with 03");
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("Enter your city");
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Enter your address");
            return;
        }

        if (cartDocs.isEmpty()) {
            Toast.makeText(this,
                    "Your cart is empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> items = new ArrayList<>();

        for (DocumentSnapshot d : cartDocs) {
            Map<String, Object> it = new HashMap<>();
            it.put("productId", d.getId());
            it.put("title", d.getString("title"));
            it.put("price", d.getLong("price"));
            it.put("quantity", d.getLong("quantity"));
            items.add(it);
        }

        Map<String, Object> addr = new HashMap<>();
        addr.put("name", name);
        addr.put("phone", phone);
        addr.put("city", city);
        addr.put("street", address);

        Map<String, Object> order = new HashMap<>();

        order.put("userId",
                FirebaseAuth.getInstance().getUid());
        order.put("items", items);
        order.put("subtotal", subtotal);
        order.put("deliveryFee", delivery);
        order.put("total", subtotal + delivery);
        order.put("address", addr);
        order.put("paymentMethod", "COD");
        order.put("status", "placed");
        order.put("createdAt",
                FieldValue.serverTimestamp());

        btnPlace.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        WriteBatch batch = db.batch();

        DocumentReference orderRef =
                db.collection("orders").document();

        batch.set(orderRef, order);

        for (DocumentSnapshot d : cartDocs) {
            batch.delete(d.getReference());
        }

        batch.commit()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Order placed! ID: " + orderRef.getId(),
                            Toast.LENGTH_LONG).show();

                    Intent i = new Intent(
                            this, ProductListActivity.class);

                    i.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    btnPlace.setEnabled(true);
                    progress.setVisibility(View.GONE);

                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}