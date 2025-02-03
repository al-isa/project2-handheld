package com.example.stocksapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var signupBtn: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        username=findViewById(R.id.usernameText)
        password=findViewById(R.id.editTextTextPassword)
        signupBtn=findViewById(R.id.signupButton)
        firebaseAuth=FirebaseAuth.getInstance()

        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        signupBtn.setOnClickListener {
            val inputtedUserName: String=username.text.toString().trim()
            val inputtedPassword: String=password.text.toString().trim()

            firebaseAuth.createUserWithEmailAndPassword(inputtedUserName,inputtedPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = firebaseAuth.currentUser
                        Toast.makeText(this, "User ${user!!.email} created successfully", Toast.LENGTH_LONG).show()
                        finish()
                    }else{
                        val exception = it.exception
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("$exception")
                            .show()
                    }
                }
        }





    }
    private val textWatcher: TextWatcher=object:TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val inputtedUsername:String=username.text.toString()
            val inputtedPassword:String=password.text.toString()

            val enableButton: Boolean=inputtedUsername.isNotBlank()&& inputtedPassword.isNotBlank()
            signupBtn.setEnabled(enableButton)
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }
}