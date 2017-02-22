package com.telecom.cottoncrosnier.logorecognition2.reference;

import java.io.File;
import java.io.Serializable;

/**
 * <p>Représente une marque. Chaque marque est identifiée par un nom, un site web, un préfixe de
 * fichier d'image et un texte d'information.</p>
 */
public class Brand implements Serializable {

    private static final String TAG = Brand.class.getSimpleName();

    private final String mBrandName;
    private final String mUrl;
    private final String mClassifier;
    private final String[] mImages;

    private File mLocalClassifier;


    /**
     * Instancie un nouvel objet Brand.
     *
     * @param brandName  nom de la marque.
     * @param url        site web de la marque.
     * @param classifier nom du fichier de classifier de la marque.
     * @param images     liste des images d'apprentissage de la marque.
     */
    public Brand(String brandName, String url, String classifier, String[] images) {
        this.mBrandName = brandName;
        this.mUrl = url;
        this.mClassifier = classifier;
        this.mImages = images;
    }

    /**
     * Renvoie le fichier de classifier de la marque stocké dans le cache.
     *
     * @param classifier fichier classifier dans le cache.
     */
    public void setLocalClassifier(File classifier) {
        this.mLocalClassifier = classifier;
    }

    /**
     * Renvoie le nom de la marque.
     *
     * @return nom de la marque.
     */
    public String getBrandName() {
        return mBrandName;
    }

    /**
     * Renvoie l'URL du site web de la marque.
     *
     * @return URL du site web.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Renvoie le nom du fichier de classifier de la marque.
     *
     * @return nom du fichier de classifier.
     */
    public String getClassifierFile() {
        return mClassifier;
    }

    /**
     * Renvoie la liste des images d'apprentissage de la marque.
     *
     * @return liste des images d'apprentissage.
     */
    public String[] getImages() {
        return mImages;
    }

    /**
     * Renvoie le nom du fichier de classifier de la marque sur le site web.
     *
     * @return nom du fichier de classifier de la marque.
     */
    public File getClassifier() {
        return mLocalClassifier;
    }

    /**
     * Renvoie une chaîne contenant les attributs de la marque.
     *
     * @return chaîne contenant les attributs de la marque.
     */
    public String toString() {
        String str = "[brandName = " + mBrandName
                + "; url = " + mUrl
                + "; classifier = " + mClassifier
                + "; images =";

        for (String image : mImages) {
            str += " " + image;
        }

        return str + "]";
    }
}
