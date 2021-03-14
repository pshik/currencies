package ru.pshik.focusstart.currencies;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableCurr);
        LayoutInflater inflater = LayoutInflater.from(this);
        TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
        TextView tv = (TextView) tr.findViewById(R.id.col1);
        tv.setText("Код");
        tv = (TextView) tr.findViewById(R.id.col2);
        tv.setText("Номинал");
        tv = (TextView) tr.findViewById(R.id.col3);
        tv.setText("Наименование");
        tv = (TextView) tr.findViewById(R.id.col4);
        tv.setText("Значение");
        tableLayout.addView(tr);
        new AsyncRequest().execute();
        Spinner spinner = (Spinner) findViewById(R.id.spinnerCurr);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) findViewById(R.id.tvResult);
                tv.setText("");
                EditText et = (EditText) findViewById(R.id.etfInput);
                et.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public class AsyncRequest extends AsyncTask<String,Integer,String>{
        private List<Valute> valuteList = new ArrayList<>();
        private ArrayList<String> charCodes = new ArrayList<>();
        @Override
        protected String doInBackground(String... strings) {
            String urlString = "https://www.cbr-xml-daily.ru/daily_json.js";
            ObjectMapper mapper = new ObjectMapper();
            try {
                URL url = new URL(urlString);
                JsonNode jsonNode = mapper.readTree(url);
                JsonNode jsonValute = jsonNode.get("Valute");

                for(JsonNode j : jsonValute){
                    Valute valute = new Valute();
                    valute.setID(j.get("ID").asText());
                    valute.setNumCode(Integer.parseInt(j.get("NumCode").asText()));
                    valute.setCharCode(j.get("CharCode").asText());
                    valute.setNominal(Integer.parseInt(j.get("Nominal").asText()));
                    valute.setName(j.get("Name").asText());
                    valute.setValue(Double.parseDouble(j.get("Value").asText()));
                    valute.setPrevious(Double.parseDouble(j.get("Previous").asText()));
                    valuteList.add(valute);
                    charCodes.add(j.get("CharCode").asText());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            for (Valute v:
                 valuteList) {
                addRow(v);

            }
            addSpinner(charCodes);
            EditText inputCurr = (EditText) findViewById(R.id.etfInput);

            inputCurr.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.length() != 0) {
                        TextView result = (TextView) findViewById(R.id.tvResult);
                        Spinner sp = (Spinner) findViewById(R.id.spinnerCurr);
                        String selectedItem = sp.getSelectedItem().toString();
                        Valute selectedValute = null;
                        for (Valute v: valuteList){
                            if (v.getCharCode() == selectedItem){
                                selectedValute = v;
                                break;
                            }
                        }
                        if(selectedValute != null){
                            int nominal = selectedValute.getNominal();
                            double value = selectedValute.getValue();
                            double rub = Double.parseDouble(charSequence.toString());
                            Double resultD = rub * nominal / value ;
                            String resultSting = String.format("%5.4g", resultD);
                            result.setText(resultSting);
                        }
                    } else{
                        TextView viewById = (TextView) findViewById(R.id.tvResult);
                        viewById.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


        }
    }

    private void addSpinner(ArrayList<String> charCodes) {
        Spinner s = (Spinner)  findViewById(R.id.spinnerCurr);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, charCodes);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(arrayAdapter);

    }

    public void addRow(Valute currency) {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableCurr);
        LayoutInflater inflater = LayoutInflater.from(this);
        TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
        TextView tv = (TextView) tr.findViewById(R.id.col1);
        tv.setText(currency.getCharCode());
        tv = (TextView) tr.findViewById(R.id.col2);
        tv.setText(Integer.toString(currency.getNominal()));
        tv = (TextView) tr.findViewById(R.id.col3);
        tv.setText(currency.getName());
        tv = (TextView) tr.findViewById(R.id.col4);
        tv.setText(Double.toString(currency.getValue()));
        tableLayout.addView(tr);
    }

}