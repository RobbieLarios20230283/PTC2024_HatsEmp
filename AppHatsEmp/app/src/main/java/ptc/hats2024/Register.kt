package ptc.hats2024

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.util.UUID

class Register : AppCompatActivity() {
    val codig_opcion_Tomar_Foto = 103
    val codig_opcion_Documento = 102
    val codig_opcion_Tomar_Foto_Dui = 101
    val CAMERA_REQUEST_CODE = 0
    val STORAGE_REQUEST_CODE = 1

    lateinit var Imgfoto: ImageView
    lateinit var myPath: String
    lateinit var txtEmail: EditText
    lateinit var txtPass: EditText
    lateinit var txtPdfDocumentName: TextView
    lateinit var ImgDui: ImageView

    val uuid = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Imgfoto = findViewById(R.id.ImgUser)
        ImgDui = findViewById(R.id.ImgDui)
        val btnTomarFot: Button = findViewById(R.id.btnTomarFoto)
        val btnTomarFotDui: Button = findViewById(R.id.btnTomarFotoDui)
        val btnIncreasePdfDocument: Button = findViewById(R.id.btnIngresarPdf)
        val btnRegist: Button = findViewById(R.id.btnRegistrarse)

        val txtNumbers: EditText = findViewById(R.id.txtNum)
        val txtCompleteName: EditText = findViewById(R.id.txtName)
        val txtDirection: EditText = findViewById(R.id.txtDirec)
        val txtConfirmPassword: EditText = findViewById(R.id.txtConfirmarCon)

        txtEmail = findViewById(R.id.txtEmail)
        txtPass = findViewById(R.id.txtPassword)
        txtPdfDocumentName = findViewById(R.id.txtPdfName)

        btnTomarFot.setOnClickListener {
            checkCameraPermission()
        }
        btnTomarFotDui.setOnClickListener {
            checkCameraPermission()
        }
        btnIncreasePdfDocument.setOnClickListener {
            checkStoragePermission()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoCamara()
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, codig_opcion_Tomar_Foto)
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoAlmacenamiento()
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, codig_opcion_Documento)
            } else {
                Toast.makeText(this, "No se pudo descargar el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pedirPermisoCamara() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
            // Mostrar explicación al usuario
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    private fun pedirPermisoAlmacenamiento() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Mostrar explicación al usuario
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, codig_opcion_Tomar_Foto)
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "application/pdf"
                    startActivityForResult(intent, codig_opcion_Documento)
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Manejar otros casos si es necesario
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                codig_opcion_Documento -> {
                    val docUri: Uri? = data?.data
                    docUri?.let { uri ->
                        val storageReference = FirebaseStorage.getInstance().reference.child("documents/${uri.lastPathSegment}")
                        val uploadTask = storageReference.putFile(uri)

                        uploadTask.addOnSuccessListener {
                            storageReference.downloadUrl.addOnSuccessListener { url ->
                                myPath = url.toString()
                                val objUrl = uri.lastPathSegment
                                val txtUrl = objUrl.toString()
                                txtPdfDocumentName.text = txtUrl
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al subir el documento: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                codig_opcion_Tomar_Foto, codig_opcion_Tomar_Foto_Dui -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let { bitmap ->
                        increaseImageFirebase(bitmap) { url ->
                            myPath = url
                            if (requestCode == codig_opcion_Tomar_Foto) {
                                Imgfoto.setImageBitmap(bitmap)
                            } else {
                                ImgDui.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun increaseImageFirebase(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("images/${uuid}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this@Register, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
        }
    }
}
