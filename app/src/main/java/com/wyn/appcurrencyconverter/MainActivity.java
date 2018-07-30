package com.wyn.appcurrencyconverter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {


    Button convert;
    TextView textView;
    RequestQueue requestQueue;
    Spinner spinner;
    Spinner spinner2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner=(Spinner)findViewById(R.id.spinner1);
        spinner2=(Spinner)findViewById(R.id.spinner2);
        final TextView textView=(TextView)findViewById(R.id.resultBox);
        Button convert = (Button)findViewById(R.id.convertBtn);
        final EditText editText = (EditText) findViewById(R.id.amountValue);

        Cache cache = new DiskBasedCache(getCacheDir(),1024*1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue=new RequestQueue(cache,network);
        requestQueue.start();
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textSpinner = spinner.getSelectedItem().toString();
                String textSpinner2= spinner2.getSelectedItem().toString();
                String value = editText.getText().toString();
                final String convertedValue =spinner2.getSelectedItem().toString();
                final Double userAmount;
                if(value!=null && value.length()>0) {
                    userAmount = Double.valueOf(value);
                }else{
                    userAmount=1.0;
                }
                //request data from server as string
                String URL ="https://exchangeratesapi.io/api/latest?base="+textSpinner+"&symbols="+textSpinner2;
                StringRequest stringRequest= new StringRequest(Request.Method.GET, URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try{
                                    //convert response to json and get value to display it
                                    JSONObject obj = new JSONObject(response);
                                    Log.e("My app",obj.toString());
                                    JSONObject ratesObj = obj.getJSONObject("rates");
                                    String currency = ratesObj.getString(convertedValue);
                                    Double currencyValue  = Double.valueOf(currency);
                                    Double result=currencyValue*userAmount;
                                    textView.setText(String.format("%.2f",result));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("error......");
                        error.printStackTrace();
                    }

                }){
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        try {
                            Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                            if (cacheEntry == null) {
                                cacheEntry = new Cache.Entry();
                            }
                            final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                            final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                            long now = System.currentTimeMillis();
                            final long softExpire = now + cacheHitButRefreshed;
                            final long ttl = now + cacheExpired;
                            cacheEntry.data = response.data;
                            cacheEntry.softTtl = softExpire;
                            cacheEntry.ttl = ttl;
                            String headerValue;
                            headerValue = response.headers.get("Date");
                            if (headerValue != null) {
                                cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                            }
                            headerValue = response.headers.get("Last-Modified");
                            if (headerValue != null) {
                                cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                            }
                            cacheEntry.responseHeaders = response.headers;
                            final String jsonString = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers));
                            return Response.success(new String(jsonString), cacheEntry);
                        } catch (UnsupportedEncodingException e) {
                            return Response.error(new ParseError(e));
                        }
                    }

                    @Override
                    protected void deliverResponse(String response) {
                        super.deliverResponse(response);
                    }

                    @Override
                    public void deliverError(VolleyError error) {
                        super.deliverError(error);
                    }

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        return super.parseNetworkError(volleyError);
                    }
                };
                requestQueue.add(stringRequest);
            }
        });
    }
}

