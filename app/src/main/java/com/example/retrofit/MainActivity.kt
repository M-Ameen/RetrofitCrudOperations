package com.example.retrofit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.retrofit.retrofit.getrequest.UserData
import com.example.retrofit.retrofit.postrequest.PostResponse
import com.example.retrofit.retrofit.postrequest.RequestPost
import com.example.retrofit.retrofit.register.RegisterModel
import com.example.retrofit.retrofit.register.RegisterResponse
import com.example.retrofit.retrofit.uploadfile.UploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    interface RequestUser {
        @GET("/api/users/{uid}")
        fun getUser(@Path("uid") uid: String): Call<UserData>

        @POST("/api/users")
        fun postUser(@Body body: RequestPost): Call<PostResponse>

        @POST("/api/register")
        fun register(@Body registerModel: RegisterModel): Call<RegisterResponse>

        @Multipart
        @POST("/api/v1/files/upload")
        fun post(@Part file: MultipartBody.Part): Call<UploadResponse>

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://reqres.in/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val requestUser = retrofit.create(RequestUser::class.java)


        //Get specific user from api
        requestUser.getUser("3").enqueue(object : Callback<UserData?> {
            override fun onResponse(p0: Call<UserData?>, response: Response<UserData?>) {
                Log.d("ameen", response.body()?.data?.email!!)
            }

            override fun onFailure(p0: Call<UserData?>, p1: Throwable) {
                Log.d("ameen", p1.message!!)
            }
        })

        //Post User to api
        requestUser.postUser(RequestPost("developer", "ameen"))
            .enqueue(object : Callback<PostResponse?> {
                override fun onResponse(
                    p0: Call<PostResponse?>,
                    response: Response<PostResponse?>
                ) {
                    Log.d("ameen", response.body()!!.job)
                    Log.d("ameen", response.body()!!.name)
                }

                override fun onFailure(p0: Call<PostResponse?>, p1: Throwable) {
                    Log.d("ameen", p1.message.toString())
                }
            })

        //Authenticate User
        requestUser.register(RegisterModel("eve.holt@reqres.in", "pistol"))
            .enqueue(object : Callback<RegisterResponse?> {
                override fun onResponse(
                    p0: Call<RegisterResponse?>,
                    response: Response<RegisterResponse?>
                ) {
                    if (response.isSuccessful) {
                        Log.d("ameen", response.body()!!.token)
                        Log.d("ameen", response.body()!!.id.toString())
                    }
                }

                override fun onFailure(p0: Call<RegisterResponse?>, p1: Throwable) {
                    Log.d("ameen", p1.message.toString())

                }
            })


        uploadImageAsFileFromDrawable()


    }

    private fun uploadImageAsFileFromDrawable() {
        val file = drawableToFile(this, R.drawable.image, "image")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.escuelajs.co")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RequestUser::class.java)

        val requestFile = file!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file?.name, requestFile)

        val call = service.post(body)

        call.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    println("File uploaded successfully:")
                    println("Original Name: ${uploadResponse?.originalname}")
                    println("Filename: ${uploadResponse?.filename}")
                    println("Location: ${uploadResponse?.location}")
                } else {
                    println("Failed to upload file: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                println("Failed to upload file: ${t.message}")
            }
        })

    }

    fun drawableToFile(context: Context, drawableResId: Int, fileName: String): File? {
        // Get the drawable from the resource ID
        val drawable = context.getDrawable(drawableResId)

        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap

            // Get the directory for storing images
            val directory =
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Drawables")

            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Create the file in the directory
            val file = File(directory, "$fileName.png")

            try {
                // Write the bitmap data to the file
                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                return file
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }


}