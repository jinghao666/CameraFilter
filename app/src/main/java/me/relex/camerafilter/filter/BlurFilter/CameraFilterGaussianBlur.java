package me.relex.camerafilter.filter.BlurFilter;

import android.content.Context;

import me.relex.camerafilter.R;
import me.relex.camerafilter.filter.CameraFilter;
import me.relex.camerafilter.filter.CameraFilterBlend;
import me.relex.camerafilter.filter.CameraFilterBlendSoftLight;
import me.relex.camerafilter.filter.FilterGroup;

public class CameraFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public CameraFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, false));
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, true));
    }
}
