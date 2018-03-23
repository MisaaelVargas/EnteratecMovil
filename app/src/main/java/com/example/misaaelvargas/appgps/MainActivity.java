package com.example.misaaelvargas.appgps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.snowdream.android.widget.SmartImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    TextView mensaje1;
    TextView mensaje2;
    private Button verMapa, verPublicidad;
    float lat, lon;

    private ListView listView;

    ArrayList titulo = new ArrayList();
    ArrayList lat1 = new ArrayList();
    ArrayList lat2 = new ArrayList();
    ArrayList long1 = new ArrayList();
    ArrayList long2 = new ArrayList();
    ArrayList descripcion = new ArrayList();
    ArrayList imagen = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mensaje1 = (TextView) findViewById(R.id.mensaje_id);
        mensaje2 = (TextView) findViewById(R.id.mensaje_id2);
        verMapa = (Button) findViewById(R.id.verMapa);
        verPublicidad = (Button) findViewById(R.id.verPublicidad);
        listView = (ListView)findViewById(R.id.listView);

        verMapa.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg8){
                Intent inten = new Intent(MainActivity.this, MapsActivity.class);

                inten.putExtra("lat", lat);
                inten.putExtra("lon", lon);

                startActivity(inten);
            }
        });

        verPublicidad.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg8){
                Intent inten = new Intent(MainActivity.this, MainActivity.class);

                descargarImagen(lat, lon);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
    }

    public void verificar(final float latActual, final float longActual){


        final String urlVariables = "http://asignaturas-ito.com/Enteratec/verificar.php";
        Toast.makeText(getApplicationContext(),urlVariables,Toast.LENGTH_LONG).show();
        final AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlVariables, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode==200){
                    try {
                        JSONArray jsonArray = new JSONArray(new String(responseBody));
                        for (int i = 0; i< jsonArray.length();i++){
                            lat1.add(jsonArray.getJSONObject(i).getString("latitud1"));
                            lat2.add(jsonArray.getJSONObject(i).getString("latitud2"));
                            long1.add(jsonArray.getJSONObject(i).getString("longitud1"));
                            long2.add(jsonArray.getJSONObject(i).getString("longitud2"));

                            if (lat1 <= latActual && lat2 >= latActual && long1 >= longActual && long2 <= longActual){
                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(MainActivity.this)
                                                .setSmallIcon(android.R.drawable.stat_sys_warning)
                                                .setLargeIcon((((BitmapDrawable)getResources()
                                                        .getDrawable(R.drawable.logo)).getBitmap()))
                                                .setContentTitle("Estas dentro de una zona")
                                                .setContentText("Abre para ver la publicidad")
                                                .setContentInfo("4")
                                                .setTicker("Alerta!");
                            }
                        }


                        listView.setAdapter(new ImagenAdapter(getApplicationContext()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void descargarImagen(float latt,float lonn){
        titulo.clear();
        descripcion.clear();
        imagen.clear();

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();

        String lats = String.valueOf(latt);
        String lons = String.valueOf(lonn);

        final String urlServ = "http://asignaturas-ito.com/Enteratec/coordenada.php?lat="+lats+"&lon="+lons;
        Toast.makeText(getApplicationContext(),urlServ,Toast.LENGTH_LONG).show();
        final AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlServ, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode==200){
                    progressDialog.dismiss();
                    try {
                        JSONArray jsonArray = new JSONArray(new String(responseBody));
                        for (int i = 0; i< jsonArray.length();i++){
                            titulo.add(jsonArray.getJSONObject(i).getString("nombre"));
                            descripcion.add(jsonArray.getJSONObject(i).getString("contenido"));
                            imagen.add(jsonArray.getJSONObject(i).getString("multimedia"));
                        }

                        listView.setAdapter(new ImagenAdapter(getApplicationContext()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    private class ImagenAdapter extends BaseAdapter{
        Context ctx;
        LayoutInflater layoutInflater;
        SmartImageView smartImageView;
        TextView tvtitulo, tvdescripcion;

        public ImagenAdapter(Context applicationContext) {
            this.ctx=applicationContext;
            layoutInflater = (LayoutInflater)ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return imagen.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = (ViewGroup)layoutInflater.inflate(R.layout.activity_main_item,null);

            smartImageView=(SmartImageView)viewGroup.findViewById(R.id.imagen1);
            tvtitulo=(TextView)viewGroup.findViewById(R.id.tvtitulo);
            tvdescripcion=(TextView)viewGroup.findViewById(R.id.tvdescripcion);

            String urlfinal = "http://asignaturas-ito.com/Enteratec/upload/"+imagen.get(position).toString();
            Rect rect = new Rect(smartImageView.getLeft(),smartImageView.getTop(),smartImageView.getRight(),smartImageView.getBottom());

            smartImageView.setImageUrl(urlfinal, rect);
            tvtitulo.setText(titulo.get(position).toString());
            tvdescripcion.setText(descripcion.get(position).toString());

            return viewGroup;
        }
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);

        mensaje1.setText("Localizacion agregada");
        mensaje2.setText("");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    mensaje2.setText("Mi direccion es: \n"
                            + DirCalle.getAddressLine(0));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* Aqui empieza la Clase Localizacion */
    class Localizacion implements LocationListener {

        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            loc.getLatitude();
            lat= (float) loc.getLatitude();
            loc.getLongitude();
            lon= (float) loc.getLongitude();

            String Text = "Mi ubicacion actual es: " + "\n Lat = "
                    + loc.getLatitude() + "\n Long = " + loc.getLongitude();
            mensaje1.setText(Text);
            this.mainActivity.setLocation(loc);

            verificar(lat , lon);
        }

        public float getLat() {
            return lat;
        }

        public float getLon() {
            return lon;
        }


        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            mensaje1.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            mensaje1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
}