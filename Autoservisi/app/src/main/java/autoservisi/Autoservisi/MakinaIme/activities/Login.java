package autoservisi.Autoservisi.MakinaIme.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import autoservisi.Autoservisi.MakinaIme.R;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    private Button login;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login=(Button)findViewById(R.id.btnlogin);
        username=(EditText)findViewById(R.id.txtuser);
        password=(EditText)findViewById(R.id.txtpass);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

    }
    public void login(){
        String user=username.getText().toString().trim();
        String pass=password.getText().toString().trim();
        if(user.equals("Admin")&& pass.equals("Admin"))
        {
            Toast.makeText(this,"Perdoruesi dhe Fjalekalimi i sakte",Toast.LENGTH_LONG).show();
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
        }else {
            Toast.makeText(this,"Perdoruesi ose Fjalekalimi nuk eshte ne rregull",Toast.LENGTH_LONG).show();
        }

    }
}
