package uk.co.createanet.footballformapp.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Matthew Grundy (Createanet) on 28/02/2014
 */
public abstract class BaseModel {

    public BaseModel() {
    }

    public BaseModel(JSONObject object) throws JSONException, IllegalAccessException {
        hydrate(object);
    }

    public static Date mysqlStringToDate(String dateString) {
        Date dateOut = null;

        try {
            dateOut = new SimpleDateFormat(/*"yyyy-MM-dd HH:mm:ss"*/ "dd/MM/yy").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateOut;
    }

    public void hydrate(JSONObject object) throws JSONException, IllegalAccessException {

        Field[] fields = getClass().getDeclaredFields();

        for (Field field : fields) {

            if(!Modifier.isFinal(field.getModifiers())) {

                if (field.getType().isAssignableFrom(boolean.class)) {

                    if (object.opt(field.getName()) instanceof String) {
                        field.set(this, object.optString(field.getName()).compareTo("Y") == 0);
                    } else {
                        field.set(this, object.optBoolean(field.getName()));
                    }

                } else if (field.getType().isAssignableFrom(int.class)) {
                    field.set(this, object.optInt(field.getName()));
                } else if (field.getType().isAssignableFrom(String.class)) {
                    field.set(this, object.optString(field.getName()));
                } else if (field.getName().compareTo("dob") == 0) {
                    if(object.optString(field.getName()) != null && object.optString(field.getName()).length() > 0){
                        field.set(this, mysqlStringToDate(object.optString(field.getName())));
                    }
                }
            }

        }

    }
}
