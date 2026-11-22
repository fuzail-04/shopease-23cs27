
package pk.edu.nfciet.shopease;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private Product product;
    private int qty = 1;
    private TextView tvQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvQty = findViewById(R.id.tvQty);

        String id = getIntent().getStringExtra("productId");

        if (id == null) {
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    product = doc.toObject(Product.class);

                    if (product == null) {
                        finish();
                        return;
                    }
                    showProduct();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Could not load product",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });

        findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (qty > 1) {
                qty--;
                tvQty.setText(String.valueOf(qty));
            }
        });

        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            if (product != null && qty < product.stock) {
                qty++;
                tvQty.setText(String.valueOf(qty));
            }
        });

        findViewById(R.id.btnAddToCart)
                .setOnClickListener(v -> addToCart());
    }

    private void showProduct() {
        getSupportActionBar().setTitle(product.title);

        ((TextView) findViewById(R.id.tvTitle))
                .setText(product.title);

        ((TextView) findViewById(R.id.tvPrice))
                .setText("Rs " + product.price);

        ((TextView) findViewById(R.id.tvDescription))
                .setText(product.description);

        ((TextView) findViewById(R.id.tvStock))
                .setText(product.stock < 5
                        ? "Only " + product.stock
                        + " left · " + product.category
                        : product.category);

        Glide.with(this)
                .load(product.imageUrl)
                .into((ImageView) findViewById(R.id.ivImage));
    }

    private void addToCart() {
        if (product == null || product.stock < 1) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> item = new HashMap<>();
        item.put("title", product.title);
        item.put("price", product.price);
        item.put("imageUrl", product.imageUrl);
        item.put("quantity", FieldValue.increment(qty));

        FirebaseFirestore.getInstance()
                .collection("carts")
                .document(uid)
                .collection("items")
                .document(product.id)
                .set(item, SetOptions.merge())
                .addOnSuccessListener(v ->
                        Toast.makeText(this,
                                "Added to cart",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}