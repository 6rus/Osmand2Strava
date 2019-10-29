package fr.rezvani.osmand2strava;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.isabsent.filepicker.SimpleFilePickerDialog;
import com.sweetzpot.stravazpot.authenticaton.api.AuthenticationAPI;
import com.sweetzpot.stravazpot.authenticaton.model.AppCredentials;
import com.sweetzpot.stravazpot.authenticaton.model.LoginResult;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity;
import com.sweetzpot.stravazpot.common.api.AuthenticationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity  implements
        SimpleFilePickerDialog.InteractionListenerString,
        SimpleFilePickerDialog.InteractionListenerInt {
    private static final String TAG = MainActivity.class.getSimpleName();
    final private int READ_EXTERNAL_STORAGE = 123;
    final private int INTERNET = 124;
    final private int RQ_LOGIN = 0;
    private String selectedFilePath;

    private String strava_token =null;

    private String client_code = null;
    public static String APP_PATH = "fr.rezvani.osmand2strava";
    private String errorResponseStrava ="not initialized";
    private String CLIENT_CODE_KEY = "fr.rezvani.osmand2strava";




    private String RIDE_NAME_CODE_KEY = "ride_name";
    private String RIDE_NUMBER_CODE_KEY = "ride_number";
    private String NP_CODE_KEY = "n_peloton";

    //New auth
    private String CODE_OAUTH ="";
    private String ACCESS_TOKEN =null;
    private String REFRESH_TOKEN = "";
    private Integer EXPIRES_AT = 0;
    private String PREF_CODE_AUTH = "PREF_CODE_AUTH";
    private String PREF_ACCESS_TOKEN = "PREF_ACCESS_TOKEN";
    private String PREF_REFRESH_TOKEN = "PREF_REFRESH_TOKEN";
    private String PREF_EXPIRES_AT =  "EXPIRES_AT";



    private String GPX_PATH_CODE_KEY = "path_gpx";
    private String SERVER_URL = "https://www.strava.com/api/v3/uploads";
    private String OAUTH_URL = "https://www.strava.com/api/v3/oauth/token";

    ListView mListView;
    Context mContext;
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("multipart/form-data; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();



    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();

        permitInternet();




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               loginStrava(view);
            }
        });

        /**  Modif auth 2019 **/
        if(getIntent().getData()!=null) {
            CODE_OAUTH = getIntent().getData().getQueryParameter("code");
            SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_CODE_AUTH, CODE_OAUTH).apply();
            Toast.makeText(this, CODE_OAUTH, Toast.LENGTH_LONG).show();

            getTokenv3();

        }
        /** fin modif 2019 **/
        readFolderinLog();




    }




    private void loginStrava(View view){



        /**   Modif auth 2019 **/
        int strava_client_id = getResources().getInteger(R.integer.strava_id);
        String client_id = String.valueOf(strava_client_id);
        Uri intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", client_id)
                .appendQueryParameter("redirect_uri", "http://rezvani.fr")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("approval_prompt", "auto")
                .appendQueryParameter("scope", "activity:write,read")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
        startActivity(intent);
        /**   Fin Modif auth 2019 **/
/**
        Intent intent = StravaLogin.withContext(getApplicationContext())
                .withClientID(15833).withRedirectURI("http://rezvani.fr").withApprovalPrompt(ApprovalPrompt.AUTO)
                .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                .makeIntent();
        startActivityForResult(intent, RQ_LOGIN);

**/

        Snackbar.make(view, "Connecte a Strava", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }



    private LoginResult getToken() {
        LoginResult result =null;



        return result;

    }


        private LoginResult getTokenOld(){


        SharedPreferences prefs = this.getSharedPreferences(
                APP_PATH, Context.MODE_PRIVATE);
        client_code =  prefs.getString(CLIENT_CODE_KEY,null);


        

        LoginResult result =null;
        try {
            AuthenticationConfig config = AuthenticationConfig.create()
                    .debug()
                    .build();
            AuthenticationAPI api = new AuthenticationAPI(config);
            String strava_key = getResources().getString(R.string.strava_key);
            int strava_client_id = getResources().getInteger(R.integer.strava_id);

           result = api.getTokenForApp(AppCredentials.with(strava_client_id,strava_key ))
                    .withCode(client_code)
                    .execute();


        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_directory) {
            showListItemDialog(R.string.title_folder, Environment.getExternalStorageDirectory().getAbsolutePath(), SimpleFilePickerDialog.CompositeMode.FOLDER_ONLY_DIRECT_CHOICE_SELECTION, "PICK_DIALOG");

        }

        return super.onOptionsItemSelected(item);
    }


    private void permitInternet(){
        int hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET},
                    INTERNET);

            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);




        if(requestCode == RQ_LOGIN && resultCode == RESULT_OK && data != null) {
            client_code = data.getStringExtra(StravaLoginActivity.RESULT_CODE);
            // sauvegarde du code client
            SharedPreferences prefs = this.getSharedPreferences(
                    APP_PATH, Context.MODE_PRIVATE);
            prefs.edit().putString(CLIENT_CODE_KEY, client_code).apply();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        refreshTokenv3();
                        /**
                        LoginResult result  = getToken();
                        if(result==null){
                            Log.d("No result","null");
                        }else {
                            strava_token =result.getToken().toString();
                        }
                        Log.d("STRAVA_TOKEN = ",strava_token); **/
                    }
                    catch(Exception e){
                        Log.d("e", e.getStackTrace().toString());
                    }
                }
            }).start();
        }
    }

    private void readFolderinLog(){
        int hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE);
            return;
        }


        Log.d("Files", "Path: " + getPath());
        File directory = new File(getPath());
        File[] files = directory.listFiles();
        if(files==null){


         }

        else{
            String[] files_names = new String[files.length];

            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {

                files_names[i]=files[i].getName();
                Log.d("Files", "FileName:" + files_names[i]);
            }

            mListView = (ListView) findViewById(R.id.liste);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, files_names);
            if(mListView!=null){
                mListView.setAdapter(adapter);
            } else{
                Log.d("test","null");
            }
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AppCompatTextView apt = (AppCompatTextView) view;
                    selectedFilePath = apt.getText().toString();

                    Log.d("filePath",selectedFilePath);

                    //   uploadFile(selectedFilePath);


                    if(ACCESS_TOKEN!=null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //creating new thread to handle Http Operations
                                uploadFile();
                            }
                        }).start();

                    } else {
                        Log.d("No token","test");
                        new GetTokenBeforePosting().execute("");
                    }


                }
            });
        }


    }



    public int getTokenv3() {


        SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
        String code_auth = prefs.getString(PREF_CODE_AUTH, null);
        if( code_auth==null){
            Log.w("ERROR ", "code_auth null ");
            return 0;

        }
          String strava_key = getResources().getString(R.string.strava_key);
          int strava_client_id = getResources().getInteger(R.integer.strava_id);
             RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                 .addFormDataPart("client_id", String.valueOf(strava_client_id))
                 .addFormDataPart("client_secret", strava_key)
                 .addFormDataPart("code", code_auth)
                 .addFormDataPart("grant_type", "authorization_code")
                .build();




            Request request = new Request.Builder()
                    .url(OAUTH_URL)
                    .post(requestBody)
                    .header("code", code_auth)
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String mMessage = e.getMessage().toString();
                    Log.w("failure Response", mMessage);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String mMessage = response.body().string();
                    try{
                        JSONObject res = new JSONObject(mMessage);


                        REFRESH_TOKEN = (String) res.get("refresh_token");
                        ACCESS_TOKEN = (String) res.get("access_token");
                        EXPIRES_AT = (Integer) res.get("expires_at");
                        Log.w("refresh_token ", REFRESH_TOKEN);
                        Log.w("access_token ", ACCESS_TOKEN);
                        Log.w("expires_at ", EXPIRES_AT.toString());

                        // sauvegarde des tokens
                        SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
                        prefs.edit().putString(PREF_ACCESS_TOKEN, ACCESS_TOKEN).apply();
                        prefs.edit().putString(PREF_REFRESH_TOKEN, REFRESH_TOKEN).apply();
                        prefs.edit().putInt(PREF_EXPIRES_AT, EXPIRES_AT).apply();



                    }catch (JSONException e){
                        Log.w("ERROR PARSING JSON", mMessage);

                    }


                    Log.w("SUCCESS Response", mMessage);

                }
          });








        return 0;
    }



    public int refreshTokenv3() {


        //  checking to see if the short-lived access token has expired
        long currenttime = System.currentTimeMillis();
        //long expires_at =  EXPIRES_AT.longValue();

        SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
        long expires_at =  prefs.getInt(PREF_EXPIRES_AT, 0);
      //  String access_token = prefs.getString(PREF_ACCESS_TOKEN, null);
        String refresh_token =  prefs.getString(PREF_REFRESH_TOKEN, null);
        String code_auth = prefs.getString(PREF_CODE_AUTH, null);

        if (expires_at<currenttime) {
            Log.w("expired : e<c",expires_at+ "<" + currenttime );

            String strava_key = getResources().getString(R.string.strava_key);
            int strava_client_id = getResources().getInteger(R.integer.strava_id);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("client_id", String.valueOf(strava_client_id))
                    .addFormDataPart("client_secret", strava_key)
                    .addFormDataPart("grant_type", "refresh_token")
                    .addFormDataPart("refresh_token", refresh_token)
                    .build();




            Request request = new Request.Builder()
                    .url(OAUTH_URL)
                    .post(requestBody)
                    .header("code", code_auth)
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String mMessage = e.getMessage().toString();
                    Log.w("failure Response", mMessage);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String mMessage = response.body().string();
                    try{
                        JSONObject res = new JSONObject(mMessage);
                        REFRESH_TOKEN = (String) res.get("refresh_token");
                        ACCESS_TOKEN = (String) res.get("access_token");
                        EXPIRES_AT = (Integer) res.get("expires_at");
                        Log.w("refresh_token ", REFRESH_TOKEN);
                        Log.w("access_token ", ACCESS_TOKEN);
                        Log.w("expires_at ", EXPIRES_AT.toString());
                        // sauvegarde des tokens
                        SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
                        prefs.edit().putString(PREF_ACCESS_TOKEN, ACCESS_TOKEN).apply();
                        prefs.edit().putString(PREF_REFRESH_TOKEN, REFRESH_TOKEN).apply();
                        prefs.edit().putInt(PREF_EXPIRES_AT, EXPIRES_AT).apply();

                    }catch (JSONException e){
                        Log.w("ERROR PARSING JSON", mMessage);

                    }


                    Log.w("SUCCESS Response", mMessage);

                }
            });



        } else {
            Log.w("not expired : e>c",expires_at+ ">" + currenttime );

        }









        return 0;
    }




    public int uploadFile() {
        try {
        /* https://github.com/square/okhttp/wiki/Recipes*/

            //BUG XIAOMI
            //String  completFilePath = Environment.getExternalStorageDirectory().toString()+  getPath() + "/" +selectedFilePath;

            String  completFilePath =  getPath() + "/" +selectedFilePath;
            String name = getRideName();



            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name", name)
                    .addFormDataPart("activity_type", "ride")
                    .addFormDataPart("file", selectedFilePath,
                            RequestBody.create(MEDIA_TYPE_MARKDOWN, new File(completFilePath)))
                    .addFormDataPart("data_type", "gpx")
                    .build();
            // sauvegarde des tokens
            SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
            String access_token =  prefs.getString(PREF_ACCESS_TOKEN, null);

            Request request = new Request.Builder()
                    .header("Authorization", "Bearer " + access_token)
                    .url(SERVER_URL)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JSONObject mainObject = new JSONObject(responseString);
            Log.d("response",responseString);

            if (!response.isSuccessful()) {



                //Let this be the code in your n'th level thread from main UI thread


                errorResponseStrava = mainObject.getString("error");

                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), errorResponseStrava,Toast.LENGTH_LONG).show();
                    }
                });

                throw new IOException("Unexpected code " + response);
            }else{


                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    public void run() {



                        String name = getRideName();
                        Toast.makeText(getApplicationContext(), "Upload successfull : " + name ,Toast.LENGTH_LONG).show();
                        incrementRideNP();

                    }
                });
            }



        } catch ( Exception e){
            e.printStackTrace();
            return 1;
        }


        return 0;
    }


    public String getPath(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        String name =  sharedPref.getString(GPX_PATH_CODE_KEY, "");

        return name;

    }

    public String getRideName(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = selectedFilePath.substring(0, selectedFilePath.lastIndexOf("."));
        if(sharedPref.getBoolean(NP_CODE_KEY,false)){
            String ride_name =  sharedPref.getString(RIDE_NAME_CODE_KEY,"PROUT");
            String ride_number =  sharedPref.getString(RIDE_NUMBER_CODE_KEY,"666");
            name = ride_name + " #" + ride_number;
        }
        return name;

    }

    public void incrementRideNP(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // incrémentation du ride
        if(sharedPref.getBoolean(NP_CODE_KEY,false)) {
            String ride_number =  sharedPref.getString(RIDE_NUMBER_CODE_KEY,"666");
            int number = Integer.parseInt(ride_number);
            number++;
            sharedPref.edit().putString(RIDE_NUMBER_CODE_KEY, "" + number).apply();
        }

    }




    private class GetTokenBeforePosting extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
            String access_token =  prefs.getString(PREF_ACCESS_TOKEN, null);
            if(access_token==null){
                getTokenv3();
            } else{
                refreshTokenv3();
            }


            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {


            SharedPreferences prefs = mContext.getSharedPreferences(  APP_PATH, Context.MODE_PRIVATE);
            String access_token =  prefs.getString(PREF_ACCESS_TOKEN, null);
            if(access_token!=null) {


                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        //creating new thread to handle Http Operations
                        uploadFile();
                    }

                }).start();

            } else {
                Log.d("No token","test");
                Toast.makeText(mContext, "No token, you must login once",Toast.LENGTH_SHORT).show();


            }

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    @Override
    public void showListItemDialog(int titleResId, String folderPath, SimpleFilePickerDialog.CompositeMode mode, String dialogTag){
        SimpleFilePickerDialog.build(folderPath, mode)
                .title(titleResId)
//                .neut("hoch")    //Some customization if you need
//                .neg("eröffnen")
//                .pos("wählen")
//                .choiceMin(1);
//                .filterable(true, true)
                .show(this, dialogTag);
    }

    @Override
    public void showListItemDialog(String title, String folderPath, SimpleFilePickerDialog.CompositeMode mode, String dialogTag){
        SimpleFilePickerDialog.build(folderPath, mode)
                .title(title)
//                .neut("hoch")    //Some customization if you need
//                .neg("eröffnen")
//                .pos("wählen")
//                .choiceMin(1);
//                .filterable(true, true)
                .show(this, dialogTag);
    }


    @Override
    public boolean onResult( String dialogTag, int which,  Bundle extras) {
        switch (dialogTag) {
            case "PICK_DIALOG":
                if (extras.containsKey(SimpleFilePickerDialog.SELECTED_SINGLE_PATH)) {
                    String selectedSinglePath = extras.getString(SimpleFilePickerDialog.SELECTED_SINGLE_PATH);
                    Toast.makeText(this, "Path selected:\n" + selectedSinglePath, Toast.LENGTH_LONG).show();
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean b=  sharedPref.edit().putString(GPX_PATH_CODE_KEY, selectedSinglePath) .commit();
                 //   Toast.makeText(mContext, "commit =" + String.valueOf(b), Toast.LENGTH_LONG).show();
                    readFolderinLog();

                } else if (extras.containsKey(SimpleFilePickerDialog.SELECTED_PATHS)){
                    List<String> selectedPaths = extras.getStringArrayList(SimpleFilePickerDialog.SELECTED_PATHS);
                    showSelectedPathsToast(selectedPaths);
                }
                break;
//            case PICK_DIALOG_OTHER:
//                //Do what you want here
//                break;
        }
        return false;
    }


    private void showSelectedPathsToast(List<String> selectedPaths){
        if (selectedPaths != null && !selectedPaths.isEmpty()){
            String selectedPathsString = "\n";
            for (String path : selectedPaths){
                selectedPathsString += path + "\n";
            //    Toast.makeText(mContext, "Path selected:" + selectedPathsString, Toast.LENGTH_LONG).show();

            }

        }
    }
}
