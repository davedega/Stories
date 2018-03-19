package com.dega.stories

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.dega.stories.util.Prefs
import com.dega.stories.util.UploadService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import javax.inject.Inject


/**
 * Created by ddelgado on 04/02/2018.
 */
class StoriesPesenter(private var context: Context,
                      var view: StoriesContract.View)
    : StoriesContract.Presenter, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    @Inject lateinit var prefs: Prefs

    var mCredential: GoogleAccountCredential

    private val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    private val REQUEST_ACCOUNT_PICKER = 1000

    private val SCOPES = arrayOf(YouTubeScopes.YOUTUBE_READONLY)

    init {
        App.component.inject(this)
        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("Presenter", "## onActivityResult !... ")

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    REQUEST_TAKE_GALLERY_VIDEO -> {
                        data?.let {
                            Log.e("Presenter", "## uri from gallery!... " + it.data)
                            val selectedImageUri = it.data
                            view.setVideoURI(selectedImageUri)
                            view.enableButton()
                        }
                    }
                    REQUEST_VIDEO_CAPTURE -> {
                        data?.let {
                            Log.e("Presenter", "## uri from video recorded!... " + it.data)
                            view.setVideoURI(it.data)
                            view.enableButton()
                        }
                    }
                    REQUEST_ACCOUNT_PICKER -> {
                        data?.let {
                            val acoName = it.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                            prefs.accountName = acoName
                            mCredential.selectedAccountName = acoName
                            getResultsFromApi()
                        }

                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                view.disableButton()
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onRequestVideoCapture()
                }
                return
            }
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onOpenGallery()
                }
            }
            REQUEST_ACCOUNTS_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectGoogleApi()
                }
            }
            REQUEST_PERMISSION_GET_ACCOUNTS -> {
                EasyPermissions.onRequestPermissionsResult(
                        requestCode, permissions, grantResults, this)
            }
        }
    }

    private fun connectGoogleApi() {
//        mGoogleApiClient = GoogleApiClient.Builder(context)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Plus.API)
//                .addScope(Plus.SCOPE_PLUS_PROFILE)
//                .build()
    }

    override fun onOpenGallery() {

        // check permission to read files

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                view.explainPermissions(context.getString(R.string.gallery_explanation),
                        { requestGalleryPermission() })
            } else {
                requestGalleryPermission()
            }
        } else {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            (context as AppCompatActivity).startActivityForResult(Intent.createChooser(intent,
                    "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
        }
    }


    override fun onRequestVideoCapture() {

        // check permission to use camera

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                    Manifest.permission.CAMERA)) {
                view.explainPermissions(context.getString(R.string.camera_explanation), { requestCameraPermission() })
            } else {
                requestCameraPermission()
            }
        } else {
            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            takeVideoIntent.resolveActivity(context.packageManager)?.let {
                (context as AppCompatActivity).startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }


    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE)

    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
    }

    private fun requestAccountPermission() {
        ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.GET_ACCOUNTS),
                REQUEST_ACCOUNTS_PERMISSION)
    }

    override fun onUploadVideo() {
        Log.e("PResesnter!!", "onUploadVideo!")


        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            view.showMessage(R.string.no_network_connection)
        } else {
            Log.e("COMO INIO!!", "PUM PAPA!")
//            MakeRequestTask(mCredential).execute()
        }
    }

    private fun upload(fileUri: Uri) {
        if (fileUri != null) {
            val uploadIntent = Intent(context, UploadService::class.java)
            uploadIntent.setData(fileUri)
            //todo mChosenAccountName
//            uploadIntent.putExtra(Constants.ACCOUNT_KEY, mChosenAccountName)
            context.startService(uploadIntent)
            view.showMessage(R.string.upload_started)
        }
    }

    private fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else
            null
    }

    private fun getImagePath(uri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = context.contentResolver.query(uri, proj, null, null, null)
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.let { it.close() }
        }
    }

    companion object {
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 60
        const val REQUEST_ACCOUNTS_PERMISSION = 50
        const val REQUEST_CAMERA_PERMISSION = 40
        const val REQUEST_TAKE_GALLERY_VIDEO = 30
        const val REQUEST_VIDEO_CAPTURE = 20
        const val REQUEST_READ_EXTERNAL_STORAGE = 10
    }


    // Callback for Google Api Client
    override fun onConnected(p0: Bundle?) {
        setProfileInfo()
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("onConnectionFailed", "onConnectionSuspended ....")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e("onConnectionFailed", "yes baby")

        connectionResult?.let {
            if (it.hasResolution()) {
                Toast.makeText(context,
                        R.string.connection_to_google_play_failed, Toast.LENGTH_SHORT)
                        .show()
                Log.e("onConnectionFailed",
                        String.format(
                                "Connection to Play Services Failed, error: %d, reason: %s",
                                connectionResult.errorCode,
                                connectionResult.toString()))
                try {
                    it.startResolutionForResult(context as Activity, 0)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("onConnectionFailed", e.toString(), e)
                }
            }
        }
    }


    // aux
    fun setProfileInfo() {
        //not sure if mGoogleapiClient.isConnect is appropriate...
//        if (!mGoogleApiClient.isConnected || Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) == null) {
//
//            Log.e("setProfileInfo", "fotos y texto")
//
////            (getView()!!.findViewById(R.id.avatar) as ImageView)
////                    .setImageDrawable(null)
////            (getView()!!.findViewById(R.id.display_name) as TextView)
////                    .setText(R.string.not_signed_in)
//        } else {
//            val currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient)
//            if (currentPerson.hasImage()) {
//                Log.e("setProfileInfo", "uno")
//                // Set the URL of the image that should be loaded into this view, and
//                // specify the ImageLoader that will be used to make the request.
////                (getView()!!.findViewById(R.id.avatar) as NetworkImageView).setImageUrl(currentPerson.image.url, mImageLoader)
//            }
//            if (currentPerson.hasDisplayName()) {
//                Log.e("setProfileInfo", "dos")
////                (getView()!!.findViewById(R.id.display_name) as TextView).text = currentPerson.displayName
//            }
//        }
    }


    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                context as Activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                context, Manifest.permission.GET_ACCOUNTS)) {

            if(prefs.accountName.isNullOrBlank()){
                (context as Activity).startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }else{
                mCredential.selectedAccountName = prefs.accountName
                getResultsFromApi()
            }

            Log.e("WAF ", "MAMASTE PERRO :/")

//            val accountName = prefs.accountName
//            if (accountName != null) {
//                mCredential.selectedAccountName = accountName
//                getResultsFromApi()
//            } else {
//                // Start a dialog from which the user can choose an account
//                (context as Activity).startActivityForResult(
//                        mCredential.newChooseAccountIntent(),
//                        REQUEST_ACCOUNT_PICKER)
//            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    context as Activity,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = connMgr!!.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            view.showMessage(R.string.no_network_connection)
        } else {
            Log.e("COMO INIO!!", "PUM PAPA!")
//            MakeRequestTask(mCredential).execute()
        }
    }

}
