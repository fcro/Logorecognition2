package com.telecom.cottoncrosnier.logorecognition2.utils;

import com.telecom.cottoncrosnier.logorecognition2.reference.Brand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Cette classe permet de parcourir le contenu du fichier json d'index des marques. Elle crée
 * une List d'objets {@link Brand} initialisés comme décrits dans le fichier.
 */
public class JsonParser {

    private static final String TAG = JsonParser.class.getSimpleName();

    private JSONObject mJsonData;


    /**
     * Instancie un JsonParser pour lire le contenu de {@link #mJsonData}.
     *
     * @param jsonData contenu du fichier d'index des marques au format json.
     */
    public JsonParser(JSONObject jsonData) {
        this.mJsonData = jsonData;
    }

    /**
     * Récupère le nom du fichier de vocabulaire.
     *
     * @return nom du fichier de vocabulaire.
     * @throws JSONException si aucun objet nommé {@code vocabulary} n'est trouvé dans {@link #mJsonData}.
     */
    public String readVocabulary() throws JSONException {
        return mJsonData.getString("vocabulary");
    }

    /**
     * Parcourt les marques de {@link #mJsonData} et les ajoute à une List sous forme d'objets {@link Brand}.
     *
     * @return List des objets {@link Brand} instanciés.
     * @throws JSONException si aucun tableau nommé {@code brands} n'est trouvé dans
     *                       {@link #mJsonData} ou si {@link #readBrand(JSONObject)} a levé une exception.
     */
    public List<Brand> readBrandArray() throws JSONException {
        List<Brand> brands = new ArrayList<Brand>();
        JSONArray jsonBrands = mJsonData.getJSONArray("brands");

        for (int i = 0; i < jsonBrands.length(); ++i) {
            brands.add(readBrand(jsonBrands.getJSONObject(i)));
        }

        return brands;
    }

    /**
     * Parcourt les attributs d'une marque pour instancier un objet {@link Brand}.
     *
     * @return objet {@link Brand} décrit dans {@link #mJsonData}.
     * @throws JSONException si un attribut de {@link Brand} est manquant ou si un attribut non
     *                       supporté est trouvé ou si {@link #readImageArray(JSONArray)} a levé une exception.
     */
    private Brand readBrand(JSONObject jsonBrand) throws JSONException {
        String brandName = null;
        String url = null;
        String classifier = null;
        String[] images = null;

        Iterator<String> keyIterator = jsonBrand.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            switch (key) {
                case "brandname":
                    brandName = jsonBrand.getString(key);
                    break;
                case "url":
                    url = jsonBrand.getString(key);
                    break;
                case "classifier":
                    classifier = jsonBrand.getString(key);
                    break;
                case "images":
                    images = readImageArray(jsonBrand.getJSONArray(key));
                    break;
                default:
                    throw new JSONException("Unsupported Brand attribute: " + key);
            }
        }

        return new Brand(brandName, url, classifier, images);
    }

    /**
     * Parcourt la liste des fichiers d'image d'une marque.
     *
     * @return liste des fichiers d'image.
     * @throws JSONException si une erreur se produit pendant la lecture de {@code jsonImages}.
     */
    private String[] readImageArray(JSONArray jsonImages) throws JSONException {
        ArrayList<String> imagesArrayList = new ArrayList<String>();

        for (int i = 0; i < jsonImages.length(); ++i) {
            imagesArrayList.add(jsonImages.getString(i));
        }

        return imagesArrayList.toArray(new String[0]);
    }
}
