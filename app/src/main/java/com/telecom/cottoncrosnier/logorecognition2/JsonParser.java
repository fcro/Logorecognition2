package com.telecom.cottoncrosnier.logorecognition2;

import android.util.JsonReader;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Cette classe permet de parcourir le fichier json d'index des marques. Elle crée une List
 * d'objets {@link Brand} initialisés selon le contenu du fichier json.
 */
public class JsonParser {

    private static final String TAG = JsonParser.class.getSimpleName();

    private JsonReader mReader;


    /**
     * Instancie un JsonParser pour lire le fichier {@code jsonFile}.
     *
     * @param jsonFile fichier d'index des marques au format json.
     * @throws IOException si {@code jsonFile} est introuvable ou si t pas supporté.
     */
    public JsonParser(File jsonFile) throws IOException {
        mReader = new JsonReader(new InputStreamReader(new FileInputStream(jsonFile), "UTF-8"));
    }


    /**
     * Récupère le nom du fichier de vocabulaire.
     *
     * @return nom du fichier de vocabulaire.
     * @throws IOException si une erreur se produit pendant la lecture du fichier.
     */
    public String readVocabulary() throws IOException {
        String vocabulary;
        mReader.beginObject();
        while (!mReader.nextName().equals("vocabulary")) ;

        vocabulary = mReader.nextString();

        mReader.endObject();
        mReader.close();
        return vocabulary;
    }

    /**
     * Parcourt les marques du fichier et les ajoute à une List sous forme d'objets {@link Brand}.
     *
     * @return List des objets {@link Brand} instanciés.
     */
    public List<Brand> readBrandArray() throws IOException {
        List<Brand> brands = new ArrayList<Brand>();

        mReader.beginObject();
        while (!mReader.nextName().equals("brands")) ;

        mReader.beginArray();
        while (mReader.hasNext()) {
            brands.add(readBrand());
        }
        mReader.endArray();

        mReader.endObject();
        mReader.close();
        return brands;
    }

    /**
     * Parcourt les attributs d'une marque pour instancier un objet {@link Brand}.
     *
     * @return objet {@link Brand} défini selon le fichier json.
     * @throws IOException si une erreur se produit pendant la lecture du fichier.
     */
    private Brand readBrand() throws IOException {
        String brandName = null;
        URL url = null;
        String classifier = null;
        String[] images = null;
        ArrayList<String> imagesArrayList = new ArrayList<String>();

        mReader.beginObject();
        while (mReader.hasNext()) {
            String name = mReader.nextName();
            if (name.equals("brandname")) {
                brandName = mReader.nextString();
            } else if (name.equals("url")) {
                url = new URL("http://" + mReader.nextString());
            } else if (name.equals("classifier")) {
                classifier = mReader.nextString();
            } else if (name.equals("images")) {
                images = readImages();
            } else {
                throw new IOException("Malformatted metadata file");
            }
        }
        mReader.endObject();

        return new Brand(brandName, url, classifier, images);
    }

    /**
     * Parcourt la liste des fichiers d'image d'une marque.
     *
     * @return liste des fichiers d'image.
     * @throws IOException si une erreur se produit pendant la lecture du fichier.
     */
    private String[] readImages() throws IOException {
        ArrayList<String> imagesArrayList = new ArrayList<String>();

        mReader.beginArray();
        while (mReader.hasNext()) {
            imagesArrayList.add(mReader.nextString());
        }
        mReader.endArray();

        return imagesArrayList.toArray(new String[0]);
    }
}
