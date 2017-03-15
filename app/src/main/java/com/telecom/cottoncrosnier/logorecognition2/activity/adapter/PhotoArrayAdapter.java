package com.telecom.cottoncrosnier.logorecognition2.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.telecom.cottoncrosnier.logorecognition2.reference.Photo;
import com.telecomlille.cottoncrosnier.logorecognition2.R;

import java.util.ArrayList;

/**
 * Class modifiant ArrayAdapter pour afficher toutes les photo dans une List sur MainActivity
 */
public class PhotoArrayAdapter extends ArrayAdapter<Photo> {

    private static final String TAG = ArrayAdapter.class.getSimpleName();

    private static final int INVALID_POSITION = -1;

    private Context context;
    private int layoutResourceId;
    private ArrayList<Photo> photoArrayList;

    private int selectedRow;

    /**
     * Constructeur, initialise les variables
     *
     * @param context context de l'activité pour afficher l'Adapter
     * @param layoutResourceId layout sur lequel est affiché l'Adapter
     * @param photoArrayList list à afficher dans l'Adapter
     */
    public PhotoArrayAdapter(Context context, int layoutResourceId,
                             ArrayList<Photo> photoArrayList) {
        super(context, layoutResourceId, photoArrayList);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.photoArrayList = photoArrayList;
        this.selectedRow = INVALID_POSITION;
    }

    /**
     * Retourne une vue qui affiche les données à une position donée.
     *
     * @param position position de la donnée a afficher.
     * @param convertView ancienne vue.
     * @param parent le parent sur lequel la vue est rattachée.
     * @return vue à la position {@code position}.
     */
    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PhotoHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PhotoHolder();
            holder.photoImage = (ImageView) row.findViewById(R.id.photo_image);
            holder.photoBrandName = (TextView) row.findViewById(R.id.photo_brandname);

            row.setTag(holder);
        } else {
            if (position == selectedRow) {
                row.setBackgroundResource(R.drawable.rounded_bg);
            } else {
                row.setBackgroundColor(Color.TRANSPARENT);
            }

            holder = (PhotoHolder) row.getTag();
        }

        Photo photo = photoArrayList.get(position);
        holder.photoImage.setImageBitmap(photo.getBitmap());
        holder.photoBrandName.setText(photo.getBrand().getBrandName());

        return row;
    }

    /**
     * Supprime une photo.
     *
     * @param photo photo à supprimer.
     */
    @Override
    public void remove(Photo photo) {
        selectedRow = INVALID_POSITION;
        super.remove(photo);
    }

    /**
     * Classe de stockage de la vue fille pour le pattern ViewHolder.
     */
    private static class PhotoHolder {
        ImageView photoImage;
        TextView photoBrandName;
    }

    /**
     * Sélectionne une ligne.
     *
     * @param selectedRow ligne sélectionnée.
     */
    public void selectRow(int selectedRow) {
        this.selectedRow = (selectedRow == this.selectedRow ? INVALID_POSITION : selectedRow);
    }
}