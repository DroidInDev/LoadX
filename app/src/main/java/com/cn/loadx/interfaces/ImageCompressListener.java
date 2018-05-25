package com.cn.loadx.interfaces;

import java.io.File;

/**
 * Created by Admin on 01-03-2018.
 */

public interface ImageCompressListener {

    void onImageCompressed(String imageCategory,File compressedImg);
}
