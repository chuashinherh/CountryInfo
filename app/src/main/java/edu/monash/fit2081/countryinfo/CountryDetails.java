package edu.monash.fit2081.countryinfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class CountryDetails extends AppCompatActivity {

    private TextView name;
    private TextView capital;
    private TextView code;
    private TextView population;
    private TextView area;
    private TextView currency;
    private TextView language;
    private TextView region;
    private ImageView countryFlag;
    private Button button;
    private String selectedCountry;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);

        getSupportActionBar().setTitle(R.string.title_activity_country_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectedCountry = getIntent().getStringExtra("country");

        name = findViewById(R.id.country_name);
        capital = findViewById(R.id.capital);
        code = findViewById(R.id.country_code);
        population = findViewById(R.id.population);
        area = findViewById(R.id.area);
        currency = findViewById(R.id.currencies);
        language = findViewById(R.id.languages);
        region = findViewById(R.id.region);

        countryFlag = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        button.setText("WIKI " + selectedCountry);


        ExecutorService executor = Executors.newSingleThreadExecutor();
        //Executor handler = ContextCompat.getMainExecutor(this);
        Handler uiHandler=new Handler(Looper.getMainLooper());



        executor.execute(() -> {
            //Background work here
            CountryInfo countryInfo = new CountryInfo();

            try {
                // Create URL
                URL webServiceEndPoint = new URL("https://restcountries.com/v2/name/" + selectedCountry); //

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) webServiceEndPoint.openConnection();

                if (myConnection.getResponseCode() == 200) {
                    //JSON data has arrived successfully, now we need to open a stream to it and get a reader
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");

                    //now use a JSON parser to decode data
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginArray(); //consume arrays's opening JSON brace
                    String keyName;
                    // countryInfo = new CountryInfo(); //nested class (see below) to carry Country Data around in
                    boolean countryFound = false;
                    while (jsonReader.hasNext() && !countryFound) { //process array of objects
                        jsonReader.beginObject(); //consume object's opening JSON brace
                        while (jsonReader.hasNext()) {// process key/value pairs inside the current object
                            keyName = jsonReader.nextName();
                            if (keyName.equals("name")) {
                                countryInfo.setName(jsonReader.nextString());
                                if (countryInfo.getName().equalsIgnoreCase(selectedCountry)) {
                                    countryFound = true;
                                }
                            } else if (keyName.equals("alpha3Code")) {
                                countryInfo.setAlpha3Code(jsonReader.nextString());
                            } else if (keyName.equals("capital")) {
                                countryInfo.setCapital(jsonReader.nextString());
                            } else if (keyName.equals("population")) {
                                countryInfo.setPopulation(jsonReader.nextInt());
                            } else if (keyName.equals("area")) {
                                countryInfo.setArea(jsonReader.nextDouble());
                            }
                            else if (keyName.equals("currencies")) {
                                jsonReader.beginArray(); //consume arrays's opening JSON brace
                                String nestedKeyName;
                                while (jsonReader.hasNext()) {
                                    jsonReader.beginObject();
                                    while (jsonReader.hasNext()) {
                                        nestedKeyName = jsonReader.nextName();
                                        if (nestedKeyName.equals("name")) {
                                            countryInfo.setCurrency(jsonReader.nextString());
                                        } else {
                                            jsonReader.skipValue();
                                        }
                                    }
                                    jsonReader.endObject();
                                }
                                jsonReader.endArray();
                            }
                            else if (keyName.equals("languages")) {
                                jsonReader.beginArray(); //consume arrays's opening JSON brace
                                String nestedKeyName;
                                while (jsonReader.hasNext()) {
                                    jsonReader.beginObject();
                                    while (jsonReader.hasNext()) {
                                        nestedKeyName = jsonReader.nextName();
                                        if (nestedKeyName.equals("name")) {
                                            countryInfo.setLanguage(jsonReader.nextString());
                                        } else {
                                            jsonReader.skipValue();
                                        }
                                    }
                                    jsonReader.endObject();
                                }
                                jsonReader.endArray();
                            }
                            else if (keyName.equals("region")) {
                                countryInfo.setRegion(jsonReader.nextString());
                            }
                            else if (keyName.equals("alpha2Code")) {
                                countryInfo.setAlpha2Code(jsonReader.nextString());
                            }
                            else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                    uiHandler.post(()->{
                        name.setText(countryInfo.getName());
                        capital.setText(countryInfo.getCapital());
                        code.setText(countryInfo.getAlpha3Code());
                        population.setText(Integer.toString(countryInfo.getPopulation()));
                        area.setText(Double.toString(countryInfo.getArea()));
                        currency.setText(countryInfo.getCurrency());
                        language.setText(countryInfo.getLanguage());
                        region.setText(countryInfo.getRegion());
                    });


                } else {
                    Log.i("INFO", "Error:  No response");
                }

                URL webServiceEndPointImage = new URL("https://flagcdn.com/144x108/" +
                        countryInfo.getAlpha2Code().toLowerCase() + ".png");
                HttpsURLConnection myConnectionImage = (HttpsURLConnection) webServiceEndPointImage.openConnection();
                if (myConnectionImage.getResponseCode() == 200) {
                    InputStream responseBody = myConnectionImage.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(responseBody);

                    uiHandler.post(() -> {
                        countryFlag.setImageBitmap(myBitmap);
                    });
                } else {
                    Log.i("INFO", "Error:  No response");
                }


                // All your networking logic should be here
            } catch (Exception e) {
                Log.i("INFO", "Error " + e.toString());
            }

        });

    }

    public void goToWiki(View v) {
        Intent intent = new Intent(this, WebWiki.class);
        intent.putExtra("countryName", selectedCountry);
        startActivity(intent);
    }


    private class CountryInfo {
        private String name;
        private String alpha3Code;
        private String capital;
        private int population;
        private double area;
        private ArrayList<String> currency = new ArrayList<>();
        private ArrayList<String> language = new ArrayList<>();
        private String region;
        private String alpha2Code;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlpha3Code() {
            return alpha3Code;
        }

        public void setAlpha3Code(String alpha3Code) {
            this.alpha3Code = alpha3Code;
        }

        public String getCapital() {
            return capital;
        }

        public void setCapital(String capital) {
            this.capital = capital;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        public double getArea() {
            return area;
        }

        public void setArea(double area) {
            this.area = area;
        }

        public String getCurrency() {
            String res = "";
            for (int i = 0; i < currency.size()-1; i++) {
                res += currency.get(i) + ",";
            }
            res += currency.get(currency.size()-1);
            return res;
        }

        public void setCurrency(String currency) {
            this.currency.add(currency);
        }

        public String getLanguage() {
            String res = "";
            for (int i = 0; i < language.size()-1; i++) {
                res += language.get(i) + ",";
            }
            res += language.get(language.size()-1);
            return res;
        }

        public void setLanguage(String language) {
            this.language.add(language);
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getAlpha2Code() {
            return alpha2Code;
        }

        public void setAlpha2Code(String alpha2Code) {
            this.alpha2Code = alpha2Code;
        }
    }
}
