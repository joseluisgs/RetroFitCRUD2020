package com.joseluisgs.retorfitcrud2020


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.joseluisgs.retorfitcrud2020.modelos.usuarios.Usuario
import com.joseluisgs.retorfitcrud2020.modelos.usuarios.UsuarioDTO
import com.joseluisgs.retorfitcrud2020.modelos.usuarios.UsuarioMapper
import com.joseluisgs.retorfitcrud2020.services.usuarios.UsuariosAPI
import com.joseluisgs.retorfitcrud2020.services.usuarios.UsuariosREST
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable


class MainActivity : AppCompatActivity() {
    private lateinit var usuariosREST: UsuariosREST
    private lateinit var usuariosList: MutableList<Usuario>
    private lateinit var adapter: UsuarioListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
    }

    /**
     * Iniciamos la IU
     */
    private fun initUI() {
        // Comprobamos la conexión a internet
        if (Utils.isOnline(applicationContext)) {
            Toast.makeText(this, "Sí estás conectado a Internet", Toast.LENGTH_SHORT).show()
            // Si lo hay nos conectamos a la Api Rest para probar
            usuariosREST = UsuariosAPI.service
            listarUsuarios()

        } else {
            Toast.makeText(this, "Es necesaria una conexión a internet para funcionar", Toast.LENGTH_SHORT).show()
        }
        // Barra de progreso
        mainProgressBar.visibility = View.INVISIBLE
        // Recycler
        usuariosRecycler.layoutManager = LinearLayoutManager(this)
        // Eventos de Botones
        // Probamos un botón para abri la actividad
        mainBtnAñadir.setOnClickListener {
            val intent = Intent(this, UsuarioActivity::class.java).apply {
                putExtra("MODO", "NUEVO")
                // Esto lo hago porque como en verdad no hay datos quw cambien para simular su cambio
                putExtra("LISTA", usuariosList as Serializable)
            }
            // Comenzamos la actividad
            startActivity(intent)
        }
        mainBtnObtener.setOnClickListener {
            if (Utils.isOnline(applicationContext))
                listarUsuarios();
        }
    }

    /**
     * Obtiene los usuarios como tarea asíncrona
     */
    private fun listarUsuarios() {
        mainProgressBar.visibility = View.VISIBLE
        // Creamos la tarea que llamará al servicio rest y la encolamos
        val call: Call<List<UsuarioDTO>> = usuariosREST.findAll()
        // Creamos una cola asincrona
        call.enqueue((object : Callback<List<UsuarioDTO>> {
            // Si fallamos
            override fun onFailure(call: Call<List<UsuarioDTO>>, t: Throwable) {
                Toast.makeText(applicationContext, "Error al eliminar: " + t.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }

            // Si tenemos exito
            override fun onResponse(call: Call<List<UsuarioDTO>>, response: Response<List<UsuarioDTO>>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Datos Obtenidos: " + response.body()?.size + ". Código Respuesta: " + response.code(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // Hacemos el parser de JSON a nuestro Modelo
                    mostrarUsuarios(response.body() as MutableList<UsuarioDTO>)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Error al obtener la lista. Código Respuesta : " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }))
        mainProgressBar.visibility = View.INVISIBLE
    }

    /**
     * Muestra los usuarios a partir de una lista
     * @param respuesta List<UsuarioDTO>
     */
    private fun mostrarUsuarios(respuesta: MutableList<UsuarioDTO>) {
        usuariosList = (UsuarioMapper.DTOToModel(respuesta) as MutableList<Usuario>)
        adapter = UsuarioListAdapter(usuariosList) {
            val intent = Intent(this, UsuarioActivity::class.java).apply {
                putExtra("MODO", "VER")
                putExtra("VALOR", it as Serializable)
                // Esto lo hago porque como en verdad no hay datos quw cambien para simular su cambio
                putExtra("LISTA", usuariosList as Serializable)
            }
            // Comenzamos la actividad
            startActivity(intent)
        }
        usuariosRecycler.adapter = adapter
        // Avismos que ha cambiado
        adapter.notifyDataSetChanged()
        usuariosRecycler.setHasFixedSize(true)
    }


}